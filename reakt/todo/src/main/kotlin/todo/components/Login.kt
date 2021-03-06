import com.github.andrewoma.react.*
import org.w3c.xhr.XMLHttpRequest
import todo.components.UserProps
import todo.components.createPersonalArea
import kotlin.browser.document

data class InputState(var email: String, var pass: String, var message: String)

val addressServer = "http://localhost:8080"

class Login : ComponentSpec<Unit, InputState>() {

    companion object {
        val factory = react.createFactory(Login())
    }

    override fun initialState(): InputState? {
        return InputState("", "", "")
    }

    override fun Component.render() {
        div ({
            onKeyDown = {
                if(it.keyCode == 13) { if(validInputs()) logIn(state.email, state.pass, null) }
            }
        }){
            if(!getCookie("id").equals("")){
                logIn(null, null, getCookie("id"))
            }else{
                input ({
                    className = "loginInput form-control"
                    placeholder = "Email"
                    onChange = {state.email = it.currentTarget.value}
                    defaultValue = state.email
                }){}
                input ({
                    type = "password"
                    className = "loginInput form-control"
                    placeholder = "Password"
                    onChange = {state.pass = it.currentTarget.value}
                    defaultValue = state.pass
                }){}
                div ({className = "divWithBtn"}) {
                    button ({
                        className = "loginBtn btn btn-success"
                        onClick = { if(validInputs()) logIn(state.email, state.pass, null) }
                    }) { text("Вход") }
                }
                div ({className = "divWithBtn"}) {
                    button ({
                        className = "loginBtn btn btn-success"
                        onClick = { if(validInputs()) registration()  }
                    }) { text("Регистрация") }
                    br { }
                    var access = "danger";
                    if(state.message.equals("Регистрация прошла успешно")) access = "primary"
                    span ({className = "label label-$access"}){ text(state.message) }
                }
            }
        }
    }

    private fun logIn(email: String?, pass: String?, id: String?) {
        var userPropertiesList: List<String>
        val req: XMLHttpRequest
        if(email.equals(null) && pass.equals(null)){
            req = XMLHttpRequest()
            req.open("GET", "$addressServer/getUserId/$id")
        }else{
            req = XMLHttpRequest()
            req.open("GET", "$addressServer/login/$email/$pass")
        }
        req.onload = {
            if(req.responseText.equals("\"loginFail\"")){
                state = InputState("", "", "Пользователя не существует")
            }else {
                userPropertiesList = req.responseText.replace("\"", "").split(",")
                document.cookie = "id = ${userPropertiesList[5]}; expires = ${js("new Date(new Date().getTime() + 60 * 1000 * 5).toUTCString()")}"
                react.render(createPersonalArea(UserProps(
                        userPropertiesList[0],//email
                        userPropertiesList[1],//password
                        userPropertiesList[2],//date
                        userPropertiesList[3],//ip
                        userPropertiesList[4],//countInput
                        userPropertiesList[5]//id
                )), document.getElementById("app")!!)
            }
        }
        req.send()
    }

    private fun registration(){
        val req = XMLHttpRequest()
        req.open("GET", "$addressServer/registration/${state.email}/${state.pass}")
        req.onload = {
            if(req.responseText.equals("\"registrationSuccess\"")){
                state = InputState("", "", "Регистрация прошла успешно")
            }else {
                state = InputState("", "", "Пользователь уже существует")
            }
        }
        req.send()
    }

    private fun validInputs():Boolean {
        val regexMail = Regex("""[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$""")
        val regexPass = Regex("""^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$""")
        if(!regexMail.containsMatchIn(state.email)) {
            state = InputState("", "", "Введите корректный Email")
            return false
        }
        if(!regexPass.containsMatchIn(state.pass)) {
            state = InputState("", "", "Пароль должен содержать 6 символов включая букву и цифру")
            return false
        }
        return true;
    }
}

fun getCookie(cname:String): String {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(i in 0..ca.size - 1) {
        var c = ca[i];
        while (c[0]==' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length,c.length);
        }
    }
    return "";
}

fun createLogin() = Login.factory(Ref(null))