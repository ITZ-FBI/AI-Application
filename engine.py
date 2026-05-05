#!/usr/bin/env python3
"""Core engine for Auto File Arranger."""

import json
import os
import shutil
import threading
from collections import defaultdict
from datetime import datetime
from pathlib import Path

DEFAULT_RULES: dict = {
    "Images": {"icon": "🖼️", "color": "#FF4081", "folder": "Images", "extensions": [".jpg", ".jpeg", ".png", ".gif", ".svg", ".bmp", ".webp", ".heic"]},
    "Documents": {"icon": "📄", "color": "#D500F9", "folder": "Documents", "extensions": [".pdf", ".doc", ".docx", ".txt", ".xlsx", ".pptx", ".csv", ".md"]},
    "Videos": {"icon": "🎬", "color": "#FF9100", "folder": "Videos", "extensions": [".mp4", ".mkv", ".mov", ".avi", ".wmv", ".webm", ".m4v"]},
    "Audio": {"icon": "🎵", "color": "#00E676", "folder": "Audio", "extensions": [".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a"]},
    "Compressed": {"icon": "🗜️", "color": "#FFEA00", "folder": "Compressed", "extensions": [".zip", ".rar", ".7z", ".tar", ".gz", ".iso"]},
    "Programs": {"icon": "⚙️", "color": "#FF3D57", "folder": "Programs", "extensions": [".exe", ".msi", ".dmg", ".pkg", ".deb", ".bat", ".sh"]},
    "Code": {"icon": "💻", "color": "#00D4FF", "folder": "Code", "extensions": [".py", ".js", ".html", ".css", ".java", ".cpp", ".json", ".xml"]},
}

APP_DIR = Path.home() / ".file_arranger_v8"
RULES_FILE = APP_DIR / "rules.json"
HISTORY_FILE = APP_DIR / "history.json"
SETTINGS_FILE = APP_DIR / "settings.json"
SESSION_LOCK = APP_DIR / "session.lock"


def ensure_app_dir() -> None:
    APP_DIR.mkdir(exist_ok=True)
    if not RULES_FILE.exists():
        RULES_FILE.write_text(json.dumps(DEFAULT_RULES, indent=2), encoding="utf-8")
    if not HISTORY_FILE.exists():
        HISTORY_FILE.write_text("[]", encoding="utf-8")
    if not SETTINGS_FILE.exists():
        SETTINGS_FILE.write_text(json.dumps({"duplicate_action": "rename", "exclusions": [], "show_hidden": False}, indent=2), encoding="utf-8")


def save_json_safe(path: Path, data) -> None:
    temp = path.with_suffix(".tmp")
    with open(temp, "w", encoding="utf-8") as file:
        json.dump(data, file, indent=2, ensure_ascii=False)
    temp.replace(path)


def load_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return None


class Engine:
    def __init__(self):
        ensure_app_dir()
        self.history_lock = threading.Lock()
        self.rules: dict = load_json(RULES_FILE) or dict(DEFAULT_RULES)
        self.history: list = load_json(HISTORY_FILE) or []
        self.settings: dict = load_json(SETTINGS_FILE) or {"duplicate_action": "rename", "exclusions": [], "show_hidden": False}
        self.target_folder = ""
        self.selected_files: set = set()
        self._last_batch: list = []
        self._running = False

    def set_target(self, path: str) -> None:
        self.target_folder = path
        self.selected_files.clear()

    def save_rules(self) -> None:
        save_json_safe(RULES_FILE, self.rules)

    def add_custom_type(self, category: str, folder: str, extension: str) -> bool:
        ext = extension.strip().lower()
        if not ext:
            return False
        if not ext.startswith("."):
            ext = f".{ext}"
        if category not in self.rules:
            self.rules[category] = {"icon": "📁", "color": "#00D4FF", "folder": folder or category, "extensions": []}
        if ext in self.rules[category]["extensions"]:
            return False
        self.rules[category]["extensions"].append(ext)
        if folder:
            self.rules[category]["folder"] = folder
        self.save_rules()
        return True

    def scan(self, deep: bool = False) -> list[tuple[str, str, int, str]]:
        if not self.target_folder:
            raise ValueError("target_folder is not set.")

        folder = Path(self.target_folder)
        ext_map = {ext.lower(): category for category, data in self.rules.items() for ext in data.get("extensions", [])}
        results = []
        iterator = folder.rglob("*") if deep else folder.iterdir()

        for file in iterator:
            if not file.is_file():
                continue
            category = ext_map.get(file.suffix.lower(), "Unknown")
            size = file.stat().st_size
            path = str(file)
            results.append((file.name, category, size, path))
            if category != "Unknown":
                self.selected_files.add(path)

        results.sort(key=lambda row: (row[1] == "Unknown", row[1], row[0].lower()))
        return results

    def organize(self, dry_run: bool = False, deep: bool = False, duplicate_action: str = "rename", progress_cb=None) -> None:
        if not self.target_folder:
            if progress_cb:
                progress_cb({"type": "error", "message": "No target folder set."})
            return

        folder = Path(self.target_folder)
        ext_map = {ext.lower(): category for category, data in self.rules.items() for ext in data.get("extensions", [])}
        files = [item for item in (folder.rglob("*") if deep else folder.iterdir()) if item.is_file()]

        moved = skipped = errors = 0
        counts: defaultdict = defaultdict(int)
        batch = []

        for index, file in enumerate(files, start=1):
            category = ext_map.get(file.suffix.lower())
            if not category:
                skipped += 1
                continue
            target_dir = folder / self.rules[category]["folder"]
            target = target_dir / file.name

            try:
                if dry_run:
                    moved += 1
                    counts[category] += 1
                else:
                    target_dir.mkdir(parents=True, exist_ok=True)
                    if target.exists() and duplicate_action == "rename":
                        n = 1
                        while target.exists():
                            target = target_dir / f"{file.stem}_{n}{file.suffix}"
                            n += 1
                    elif target.exists() and duplicate_action == "skip":
                        skipped += 1
                        continue
                    shutil.move(str(file), str(target))
                    moved += 1
                    counts[category] += 1
                    entry = {"from": str(file), "to": str(target), "time": datetime.now().isoformat(), "category": category}
                    batch.append(entry)
                    with self.history_lock:
                        self.history.append(entry)
            except Exception as err:
                errors += 1
                if progress_cb:
                    progress_cb({"type": "error", "message": str(err)})

            if progress_cb:
                progress_cb({"type": "progress", "current": index, "total": len(files), "counts": dict(counts)})

        if not dry_run and batch:
            self._last_batch = batch
            with self.history_lock:
                save_json_safe(HISTORY_FILE, self.history)

        if progress_cb:
            progress_cb({"type": "done", "moved": moved, "skipped": skipped, "errors": errors, "counts": dict(counts)})
