package app

import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.joinClasses
import dev.fritz2.core.render
import dev.fritz2.core.storeOf
import dev.fritz2.remote.decoded
import dev.fritz2.remote.http
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable

val nameStore = storeOf("Emily", job = Job())


@Serializable
data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

object TestApiStore : RootStore<Todo?>(null, job = Job()) {

    private val testApi = http("https://jsonplaceholder.typicode.com")
        .acceptJson().contentType("application/json")

    val retrieve = handle<Int> { _, id ->
        val resp = testApi.get("/todos/$id")
        require(resp.ok)
        return@handle resp.decoded<Todo>()
    }
}
fun main() {

    render {
        div("w-screen h-screen bg-red-500") {
            h1("text-white py-4 text-4xl") {
                +"Hello, "
                nameStore.data.renderText()
            }
            testComponent()
            h1("text-white py-4 text-4xl") {
                +"Data from API: "
                TestApiStore.data.render {
                   +(it?.title ?: "No data")
                }
            }
            button(ButtonType.SECONDARY, "Request data", onClick = {
                TestApiStore.retrieve(2)
            })
        }
    }
}

fun RenderContext.testComponent() {
    div("flex gap-3 flex-col w-1/2") {
        button(ButtonType.PRIMARY, "Change name to Maga", onClick = {
            nameStore.update("Maga")
        })
        button(ButtonType.SECONDARY, "Change name to Emily", onClick = {
            nameStore.update("Emily")
        })
    }
}

fun RenderContext.button(type: ButtonType, text: String, onClick: () -> Unit = {}) {
    button(joinClasses("text-white px-4 py-2 rounded transition hover:opacity-80", type.bgColor)) {
        +text
        clicks handledBy {
            onClick()
        }
    }
}

enum class ButtonType(val bgColor: String) {
    PRIMARY("bg-green-500"), SECONDARY("bg-yellow-500")
}