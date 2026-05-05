#!/usr/bin/env python3
"""Tkinter UI for Auto File Arranger."""

import threading
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure

from engine import Engine


class FileArrangerUI:
    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("Auto File Arranger")
        self.root.geometry("1100x720")

        self.engine = Engine()
        self.current_scan = []
        self.theme = tk.StringVar(value="Light")
        self.deep_scan = tk.BooleanVar(value=False)
        self.filter_type = tk.StringVar(value="All")

        self._build_ui()
        self._apply_theme()

    def _build_ui(self):
        top = ttk.Frame(self.root, padding=10)
        top.pack(fill="x")

        self.path_var = tk.StringVar()
        ttk.Entry(top, textvariable=self.path_var).pack(side="left", fill="x", expand=True)
        ttk.Button(top, text="Browse", command=self._browse).pack(side="left", padx=6)
        ttk.Button(top, text="Scan", command=self.scan).pack(side="left", padx=6)
        ttk.Button(top, text="Organize", command=self.organize).pack(side="left", padx=6)

        opts = ttk.Frame(self.root, padding=(10, 0, 10, 8))
        opts.pack(fill="x")
        ttk.Checkbutton(opts, text="Deep scan", variable=self.deep_scan).pack(side="left")
        ttk.Label(opts, text="Filter:").pack(side="left", padx=(12, 4))
        self.filter_combo = ttk.Combobox(opts, textvariable=self.filter_type, state="readonly", width=20)
        self.filter_combo.pack(side="left")
        self.filter_combo.bind("<<ComboboxSelected>>", lambda _: self._refresh_table())
        ttk.Label(opts, text="Theme:").pack(side="left", padx=(12, 4))
        ttk.Combobox(opts, textvariable=self.theme, values=["Light", "Dark"], width=8, state="readonly").pack(side="left")
        ttk.Button(opts, text="Apply Theme", command=self._apply_theme).pack(side="left", padx=6)

        middle = ttk.Panedwindow(self.root, orient="horizontal")
        middle.pack(fill="both", expand=True, padx=10, pady=4)

        left = ttk.Frame(middle)
        right = ttk.Frame(middle)
        middle.add(left, weight=3)
        middle.add(right, weight=2)

        self.tree = ttk.Treeview(left, columns=("name", "category", "size", "path"), show="headings")
        for c, t, w in [("name", "Name", 220), ("category", "Type", 100), ("size", "Size", 90), ("path", "Path", 500)]:
            self.tree.heading(c, text=t)
            self.tree.column(c, width=w, anchor="w")
        self.tree.pack(fill="both", expand=True)

        add_frame = ttk.LabelFrame(right, text="Add Custom File Type", padding=10)
        add_frame.pack(fill="x", pady=6)
        self.cat_var = tk.StringVar()
        self.folder_var = tk.StringVar()
        self.ext_var = tk.StringVar()
        for lbl, var in [("Category", self.cat_var), ("Folder Name", self.folder_var), ("Extension (.abc)", self.ext_var)]:
            ttk.Label(add_frame, text=lbl).pack(anchor="w")
            ttk.Entry(add_frame, textvariable=var).pack(fill="x", pady=(0, 6))
        ttk.Button(add_frame, text="Add", command=self.add_custom).pack(fill="x")

        self.figure = Figure(figsize=(4, 3), dpi=100)
        self.ax = self.figure.add_subplot(111)
        self.canvas = FigureCanvasTkAgg(self.figure, master=right)
        self.canvas.get_tk_widget().pack(fill="both", expand=True, pady=8)

        self.status = tk.StringVar(value="Ready")
        ttk.Label(self.root, textvariable=self.status, anchor="w", padding=8).pack(fill="x")

    def _browse(self):
        folder = filedialog.askdirectory()
        if folder:
            self.path_var.set(folder)
            self.engine.set_target(folder)

    def _human_size(self, size: int) -> str:
        units = ["B", "KB", "MB", "GB", "TB"]
        val = float(size)
        for u in units:
            if val < 1024:
                return f"{val:.1f} {u}"
            val /= 1024
        return f"{val:.1f} PB"

    def scan(self):
        if not self.path_var.get().strip() or not Path(self.path_var.get()).exists():
            messagebox.showerror("Error", "Select a valid folder first.")
            return
        self.engine.set_target(self.path_var.get().strip())
        self.current_scan = self.engine.scan(deep=self.deep_scan.get())
        categories = sorted({r[1] for r in self.current_scan})
        self.filter_combo["values"] = ["All"] + categories
        self.filter_type.set("All")
        self._refresh_table()
        self._draw_pie()
        self.status.set(f"Scan complete. {len(self.current_scan)} files found.")

    def _refresh_table(self):
        for row in self.tree.get_children():
            self.tree.delete(row)
        active = self.filter_type.get()
        for name, category, size, path in self.current_scan:
            if active != "All" and category != active:
                continue
            self.tree.insert("", "end", values=(name, category, self._human_size(size), path))

    def _draw_pie(self):
        counts = {}
        for _, category, _, _ in self.current_scan:
            counts[category] = counts.get(category, 0) + 1
        self.ax.clear()
        if counts:
            self.ax.pie(list(counts.values()), labels=list(counts.keys()), autopct="%1.1f%%", startangle=90)
        self.ax.set_title("File Type Distribution")
        self.canvas.draw_idle()

    def organize(self):
        def run():
            def on_event(event):
                if event["type"] == "progress":
                    self.status.set(f"Organizing... {event['current']}/{event['total']}")
                elif event["type"] == "done":
                    self.status.set(f"Done. moved={event['moved']} skipped={event['skipped']} errors={event['errors']}")
                    self.scan()
            self.engine.organize(dry_run=False, deep=self.deep_scan.get(), progress_cb=on_event)

        threading.Thread(target=run, daemon=True).start()

    def add_custom(self):
        category = self.cat_var.get().strip()
        folder = self.folder_var.get().strip()
        ext = self.ext_var.get().strip()
        if not category or not ext:
            messagebox.showwarning("Missing", "Category and extension are required.")
            return
        ok = self.engine.add_custom_type(category, folder or category, ext)
        if ok:
            self.status.set(f"Added {ext} to {category}.")
            self.cat_var.set("")
            self.folder_var.set("")
            self.ext_var.set("")
        else:
            messagebox.showinfo("Info", "Extension already exists for this category.")

    def _apply_theme(self):
        dark = self.theme.get() == "Dark"
        bg = "#1e1e1e" if dark else "#f8f9fb"
        fg = "#f5f5f5" if dark else "#1f2937"
        self.root.configure(bg=bg)
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("TFrame", background=bg)
        style.configure("TLabel", background=bg, foreground=fg)
        style.configure("TLabelframe", background=bg, foreground=fg)
        style.configure("TLabelframe.Label", background=bg, foreground=fg)


def main():
    root = tk.Tk()
    FileArrangerUI(root)
    root.mainloop()


if __name__ == "__main__":
    main()
