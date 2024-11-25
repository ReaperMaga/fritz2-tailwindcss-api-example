package com.github.reapermaga.fritz2

import dev.fritz2.core.RenderContext
import dev.fritz2.core.RootStore
import dev.fritz2.core.joinClasses
import dev.fritz2.core.render
import dev.fritz2.core.storeOf
import dev.fritz2.remote.decoded
import dev.fritz2.remote.http
import dev.fritz2.routing.routerOf
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

val router = routerOf("first")
fun main() {
    render {
        navBar()
        router.data.render { site ->
            when (site) {
                "first" -> firstSite()
                "second" -> secondSite()
                else -> div { +"Unknown site" }
            }
        }
    }
}

fun RenderContext.navBar() {
    div("w-screen h-20 bg-green-600 text-white flex gap-5 items-center justify-center text-2xl") {
        navBarLink("First site", "first")
        navBarLink("Second site", "second")
    }
}

fun RenderContext.navBarLink(text: String, site: String) {
    a("cursor-pointer transition hover:text-gray-300") {
        +text
        clicks handledBy {
            router.navTo(site)
        }
    }
}

fun RenderContext.secondSite() {
    val tabStore = storeOf("Tab 1", job = Job())
    div("w-screen h-1/2 bg-blue-500 flex gap-2") {
        button(ButtonType.PRIMARY, "Tab 1", onClick = {
            tabStore.update("Tab 1")
        })
        button(ButtonType.SECONDARY, "Tab 2", onClick = {
            tabStore.update("Tab 2")
        })
        tabStore.data.render {
            if(it == "Tab 1") {
                div("text-white py-4 text-4xl") {
                    +"Tab 1"
                }
            } else {
                div("text-white py-4 text-4xl") {
                    +"Tab 2"
                }
            }
        }
    }
}

fun RenderContext.firstSite() {
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