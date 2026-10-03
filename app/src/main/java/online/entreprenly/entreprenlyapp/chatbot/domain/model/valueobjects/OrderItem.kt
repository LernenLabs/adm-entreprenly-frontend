package online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects

data class OrderItem(val productName: String, val quantity: Int, val unitPrice: Double) {
    val subtotal: Double get() = quantity * unitPrice
}
