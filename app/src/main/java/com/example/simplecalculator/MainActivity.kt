package com.example.simplecalculator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.Context


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        val view: TextView = findViewById(R.id.view)
        val numbers = listOf(R.id.n0, R.id.n1,
            R.id.n2, R.id.n3, R.id.n4, R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9, R.id.dot)
        var actions = listOf(R.id.plus, R.id.sub, R.id.mul, R.id.divide)
        val calculator = Calculator()
        val equal:Button = findViewById(R.id.equal)
        val reset:Button = findViewById(R.id.c)
        val remove:Button = findViewById(R.id.remove)
        var flag = false
        val operators = listOf("+", "-", "*", "/")

        remove.setOnClickListener{
            calculator.view = calculator.view.dropLast(1);
            view.text = calculator.view
        }

        reset.setOnClickListener{
            calculator.reset()
            view.text = "0"
        }
        equal.setOnClickListener{
            calculator.calculate()
            view.text = calculator.view
            flag = true
        }

        val number = View.OnClickListener { buttonView ->
            val button = buttonView as Button
            var text = button.text.toString()
            if (flag && text !in operators) calculator.reset()

            calculator.getNumbers(text)
            view.text = calculator.view
            flag = false
        }

        for (buttonId in numbers) {
            findViewById<Button>(buttonId).setOnClickListener(number)
        }

        for (buttonId in actions) {
            findViewById<Button>(buttonId).setOnClickListener(number)
        }


    }
}

enum class ErrorState(val message: String) {
    NONE(""),
    DIVIDE_BY_ZERO("Error: Cannot divide by zero"),
    INVALID_EXPRESSION("Error: Invalid expression"),
    UNKNOWN_ERROR("Error: Unknown error")
}


class Calculator {
    var view = ""
    var errorState: ErrorState = ErrorState.NONE

    fun getNumbers(number: String) {
        if (errorState == ErrorState.NONE)this.view += number
        else {
            this.view = number
            errorState = ErrorState.NONE
        }
    }

    fun calculate() {
        if (this.view.contains("/0")) {
            errorState = ErrorState.DIVIDE_BY_ZERO
            this.view = errorState.message
            return
        }

        val result = evaluateExpression(this.view)

        when {
            result is Double -> {
                this.view = result.toString()
                errorState = ErrorState.NONE
            }
            result is String -> {
                // If the result is a string, determine the specific error.
                errorState = when {
                    result.contains("Error:") -> ErrorState.UNKNOWN_ERROR
                    else -> ErrorState.INVALID_EXPRESSION
                }
                this.view = errorState.message
            }
            else -> {
                errorState = ErrorState.INVALID_EXPRESSION
                this.view = errorState.message
            }
        }
    }



    fun reset() {
        this.view = ""
        errorState = ErrorState.NONE
    }
}


fun evaluateExpression(expression: String): Any? {
    if (expression.contains("/0")) {
        return "Error: Cannot divide by zero"
    }

    val rhino = Context.enter()
    rhino.optimizationLevel = -1
    return try {
        val scope: Scriptable = rhino.initStandardObjects()
        rhino.evaluateString(scope, expression, "JavaScript", 1, null)
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: Invalid expression"
    } finally {
        Context.exit()
    }
}
