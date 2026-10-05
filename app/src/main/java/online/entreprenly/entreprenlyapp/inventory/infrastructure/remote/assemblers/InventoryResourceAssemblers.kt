package online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertSeverity
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertType
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateUnitLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateUnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateWeightLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateWeightProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.StockAlertResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UpdateUnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UpdateWeightProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightProductResource

private fun String?.toInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }

private val InstantFormat: DateTimeFormatter =
    DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)

fun Instant.toResourceString(): String = InstantFormat.format(this)

// ---- Products: resource -> domain ----

fun UnitProductResource.toEntity() = Product(
    type = ProductType.UNIT,
    id = id,
    name = name,
    description = description,
    codeQR = codeQR,
    price = price,
    weightGrams = weightGrams,
    brand = brand
)

fun WeightProductResource.toEntity() = Product(
    type = ProductType.WEIGHT,
    id = id,
    name = name,
    description = description,
    codeQR = codeQR,
    price = pricePerKg,
    weightGrams = 0.0,
    brand = null
)

// ---- Products: command -> resource ----

fun CreateProductCommand.toUnitResource() = CreateUnitProductResource(
    name = name.trim(),
    description = null,
    codeQR = null,
    price = price,
    weightGrams = 0.0,
    brand = null
)

fun CreateProductCommand.toWeightResource() = CreateWeightProductResource(
    name = name.trim(),
    description = null,
    codeQR = null,
    pricePerKg = price
)

fun UpdateProductCommand.toUnitResource() = UpdateUnitProductResource(
    name = name.trim(),
    description = description,
    codeQR = codeQR,
    price = price,
    weightGrams = weightGrams,
    brand = brand
)

fun UpdateProductCommand.toWeightResource() = UpdateWeightProductResource(
    name = name.trim(),
    description = description,
    codeQR = codeQR,
    pricePerKg = price
)

// ---- Lots: resource -> domain ----

fun UnitLotResource.toEntity() = Lot(
    type = ProductType.UNIT,
    id = id,
    productId = productId,
    codeQR = codeQR,
    entryDate = entryDate.toInstantOrNull() ?: Instant.EPOCH,
    expiryDate = expiryDate.toInstantOrNull(),
    quantityUnits = quantity,
    quantityKg = 0.0
)

fun WeightLotResource.toEntity() = Lot(
    type = ProductType.WEIGHT,
    id = id,
    productId = productId,
    codeQR = codeQR,
    entryDate = entryDate.toInstantOrNull() ?: Instant.EPOCH,
    expiryDate = null,
    quantityUnits = 0,
    quantityKg = quantityKg
)

// ---- Lots: command -> resource ----

fun CreateLotCommand.toUnitResource() = CreateUnitLotResource(
    productId = productId,
    codeQR = codeQR?.takeIf { it.isNotBlank() },
    entryDate = entryDate.toResourceString(),
    quantity = quantityUnits,
    expiryDate = expiryDate?.toResourceString()
)

fun CreateLotCommand.toWeightResource() = CreateWeightLotResource(
    productId = productId,
    codeQR = codeQR?.takeIf { it.isNotBlank() },
    entryDate = entryDate.toResourceString(),
    quantityKg = quantityKg
)

// ---- Alerts: resource -> domain ----

fun StockAlertResource.toEntity() = StockAlert(
    id = id,
    lotId = lotId,
    productId = productId,
    productType = ProductType.fromName(productType),
    productName = productName,
    alertType = AlertType.fromValue(alertType),
    severity = AlertSeverity.fromValue(severity),
    createdAt = createdAt.toInstantOrNull()
)
