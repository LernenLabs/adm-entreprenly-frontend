package online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects

/** State of the WhatsApp bridge: [qr] is the pairing payload while not yet connected. */
data class WhatsAppConnection(val connected: Boolean, val qr: String?)
