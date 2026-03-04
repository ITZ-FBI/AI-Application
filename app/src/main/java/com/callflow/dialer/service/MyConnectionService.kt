package com.callflow.dialer.service

import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import com.callflow.dialer.util.TelecomUtils

class MyConnectionService : ConnectionService() {
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val number = request.address?.schemeSpecificPart.orEmpty()
        if (TelecomUtils.isEmergencyNumber(number)) {
            TelecomUtils.routeEmergencyToSystemDialer(this, number)
            return Connection.createFailedConnection(
                DisconnectCause(DisconnectCause.ERROR, "Emergency routed to system dialer")
            )
        }

        return CallFlowConnection().apply {
            setAddress(request.address, PRESENTATION_ALLOWED)
            setInitialized()
            setDialing()
            setActive()
            connectionProperties = PROPERTY_SELF_MANAGED
            setConnectionCapabilities(
                CAPABILITY_SUPPORT_HOLD or
                    CAPABILITY_HOLD or
                    CAPABILITY_MERGE_CONFERENCE or
                    CAPABILITY_SWAP_CONFERENCE
            )
        }
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection = CallFlowConnection().apply {
        setRinging()
        setAddress(request.address, PRESENTATION_ALLOWED)
    }
}

class CallFlowConnection : Connection() {
    override fun onAnswer(videoState: Int) = setActive()
    override fun onReject() = setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    override fun onDisconnect() = setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
    override fun onAbort() = setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
    override fun onHold() = setOnHold()
    override fun onUnhold() = setActive()

    override fun onCallAudioStateChanged(state: android.telecom.CallAudioState?) {
        super.onCallAudioStateChanged(state)
    }

    override fun onExtrasChanged(extras: Bundle?) {
        super.onExtrasChanged(extras)
    }
}
