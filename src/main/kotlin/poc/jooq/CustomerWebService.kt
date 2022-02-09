package poc.jooq

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import poc.jooq.generated.db.tables.records.CustomerHistoryRecord
import poc.jooq.generated.db.tables.records.CustomerRecord
import java.sql.Timestamp
import java.time.LocalDateTime

@RestController
@Transactional
@RequestMapping("customers")
class CustomerWebService(
    private val customers: CustomerRepository,
    private val customerHistories: CustomerHistoryRepository
) {

    data class CustomerWriteDto(
        val firstname: String?,
        val lastname: String?,
        val phoneNumber: String?,
        val email: String?
    )

    data class CustomerReadDto(
        val id: Long,
        val firstname: String?,
        val lastname: String?,
        val phoneNumber: String?,
        val email: String?,
        val createDate: LocalDateTime?,
        val updateDate: LocalDateTime?,
    )

    data class CustomerHistoryDto(
        val firstname: String?,
        val lastname: String?,
        val phoneNumber: String?,
        val email: String?,
        val version: Long?,
        val timestamp: LocalDateTime?,
    )

    data class RecordDto(
        val fields: List<FieldDto>
    )

    data class FieldDto(
        val name: String,
        val value: Any?
    )

    data class FieldValueDto(
        val timestamp: LocalDateTime?,
        val value: Any?
    )

    @GetMapping
    fun getAll(): List<CustomerReadDto> {
        return customers.findAll()
            .map { it.toDto() }
    }

    @GetMapping("{id}")
    fun getOne(
        @PathVariable id: Long
    ): CustomerReadDto {
        return customers.find(id).toDto()
    }

    @GetMapping("{id}/full-history")
    fun getFullHistory(
        @PathVariable id: Long,
    ): List<CustomerHistoryDto> {
        return customerHistories.findAllFor(id).map { it.toDto() }
    }

    @GetMapping("{id}/history")
    fun getHistory(
        @PathVariable id: Long
    ): List<RecordDto> {
        return customerHistories.findDistinctFor(id).map { record ->
            val fields = record.fields().map {
                FieldDto(
                    name = it.name,
                    value = it.getValue(record)
                )
            }

            RecordDto(
                fields = fields
            )
        }
    }

    @GetMapping("{id}/field-history")
    fun getFieldsHistory(
        @PathVariable id: Long
    ): MutableMap<String, MutableList<FieldValueDto>> {
        val histories = customerHistories.findAllFor(id)

        val firstnames = mutableListOf<FieldValueDto>()
        val lastnames = mutableListOf<FieldValueDto>()
        val emails = mutableListOf<FieldValueDto>()
        val phoneNumbers = mutableListOf<FieldValueDto>()

        histories.forEach { history ->
            firstnames.add(
                FieldValueDto(
                    timestamp = history.timestamp.toLocalDateTime(),
                    value = history.firstname
                )
            )
            lastnames.add(
                FieldValueDto(
                    timestamp = history.timestamp.toLocalDateTime(),
                    value = history.lastname
                )
            )
            emails.add(
                FieldValueDto(
                    timestamp = history.timestamp.toLocalDateTime(),
                    value = history.email
                )
            )
            phoneNumbers.add(
                FieldValueDto(
                    timestamp = history.timestamp.toLocalDateTime(),
                    value = history.phoneNumber
                )
            )
        }

        val firstnamesDedup = mutableListOf<FieldValueDto>()
        firstnames.reverse()
        firstnames.forEach {
            if (firstnamesDedup.size == 0 || firstnamesDedup.last().value != it.value) {
                firstnamesDedup.add(it)
            }
        }
        firstnamesDedup.reverse()

        val lastnamesDedup = mutableListOf<FieldValueDto>()
        lastnames.reverse()
        lastnames.forEach {
            if (lastnamesDedup.size == 0 || lastnamesDedup.last().value != it.value) {
                lastnamesDedup.add(it)
            }
        }
        lastnamesDedup.reverse()

        val emailsDedup = mutableListOf<FieldValueDto>()
        emails.reverse()
        emails.forEach {
            if (emailsDedup.size == 0 || emailsDedup.last().value != it.value) {
                emailsDedup.add(it)
            }
        }
        emailsDedup.reverse()

        val phoneNumbersDedup = mutableListOf<FieldValueDto>()
        phoneNumbers.reverse()
        phoneNumbers.forEach {
            if (phoneNumbersDedup.size == 0 || phoneNumbersDedup.last().value != it.value) {
                phoneNumbersDedup.add(it)
            }
        }
        phoneNumbersDedup.reverse()

        return mutableMapOf(
            "firstname" to firstnamesDedup,
            "lastname" to lastnamesDedup,
            "email" to emailsDedup,
            "phone_number" to phoneNumbersDedup
        )
    }

    @PostMapping
    fun create(
        @RequestBody body: CustomerWriteDto
    ): CustomerReadDto {
        val customer = customers.new()
        body.writeChangesTo(customer)
        customer.store()
        customer.refresh()
        customerHistories.save(customer)
        return customer.toDto()
    }

    @PutMapping("{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody body: CustomerWriteDto
    ): CustomerReadDto {
        val customer = customers.find(id)
        body.writeChangesTo(customer)
        customer.store()
        customer.refresh()
        customerHistories.save(customer)
        return customer.toDto()
    }

    private fun CustomerWriteDto.writeChangesTo(customer: CustomerRecord) {
        customer.firstname = firstname
        customer.lastname = lastname
        customer.phoneNumber = phoneNumber
        customer.email = email
    }

    private fun CustomerRecord.toDto(): CustomerReadDto {
        return CustomerReadDto(
            id = id,
            firstname = firstname,
            lastname = lastname,
            phoneNumber = phoneNumber,
            email = email,
            createDate = createTimestamp.toLocalDateTime(),
            updateDate = updateTimestamp.toLocalDateTime()
        )
    }

    private fun CustomerHistoryRecord.toDto(): CustomerHistoryDto {
        return CustomerHistoryDto(
            firstname = firstname,
            lastname = lastname,
            phoneNumber = phoneNumber,
            email = email,
            timestamp = timestamp.toLocalDateTime(),
            version = version
        )
    }
}