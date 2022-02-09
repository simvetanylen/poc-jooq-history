package poc.jooq

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import poc.jooq.generated.db.tables.records.CustomerRecord
import java.time.LocalDateTime

@RestController
@Transactional
@RequestMapping("customers")
class CustomerWebService(
    private val customers: CustomerRepository
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

    @PostMapping
    fun create(
        @RequestBody body: CustomerWriteDto
    ): CustomerReadDto {
        val customer = customers.new()
        body.writeChangesTo(customer)
        customer.store()
        customer.refresh()
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
}