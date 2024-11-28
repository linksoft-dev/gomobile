package app

import (
	"fmt"
	"net/http"
	"sync"
)

var (
	counter int
	mutex   sync.Mutex
)

func App() {
	http.HandleFunc("/", ServeHTML)
	http.HandleFunc("/increment", IncrementValue)

	fmt.Println("Servidor iniciado em http://localhost:8080")
	http.ListenAndServe(":8080", nil)
}

func ServeHTML(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/html")
	fmt.Fprintf(w, `
		<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contador com HTMX</title>
    <script src="https://unpkg.com/htmx.org"></script>
</head>
<body>
    <h1>Contador com HTMX11</h1>
    
    <p>Valor atual: <span id="counter">%d</span></p>
    
    <button hx-post="/increment" hx-target="#counter" hx-swap="outerHTML" id="increment-btn">Incrementar</button>
    
    <!-- Elemento para exibir os logs -->
    <div id="log"></div>

    <script>
        // Log de eventos HTMX
        document.body.addEventListener('htmx:beforeRequest', function(event) {
            logRequest('Iniciando requisição para: ' + event.target.getAttribute('hx-post') || event.target.getAttribute('hx-get'));
        });

        document.body.addEventListener('htmx:configRequest', function(event) {
            logRequest('Configuração da requisição: Método - ' + event.detail.verb + ', URL - ' + event.detail.url);
        });

        document.body.addEventListener('htmx:requestError', function(event) {
            logRequest('Erro na requisição: ' + event.detail.xhr.statusText);
        });

        document.body.addEventListener('htmx:afterRequest', function(event) {
            logRequest('Requisição completada. Status: ' + event.detail.xhr.status);
        });

        document.body.addEventListener('htmx:responseError', function(event) {
            logRequest('Erro na resposta: ' + event.detail.xhr.responseText);
        });

        function logRequest(message) {
            const logDiv = document.getElementById('log');
            const logEntry = document.createElement('p');
            logEntry.textContent = message;
            logDiv.appendChild(logEntry);
        }
    </script>
</body>
</html>

	`, counter)
}

func IncrementValue(w http.ResponseWriter, r *http.Request) {
	// Bloqueio para evitar condições de corrida
	mutex.Lock()
	counter++
	newValue := counter
	mutex.Unlock()

	// Retorna apenas o novo valor como resposta HTML
	w.Header().Set("Content-Type", "text/html")
	fmt.Fprintf(w, `<span id="counter">%d</span>`, newValue)
}
