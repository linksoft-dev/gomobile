package com.example.app;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.*;
import com.getcapacitor.BridgeActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obter o endereço IP do dispositivo
        String deviceIp = getDeviceIp();
        startGoServer();

        // Testar a conexão com o servidor Go
        testServerConnection();

        // Carregar o WebView
        WebView webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                super.onConsoleMessage(consoleMessage);
                Log.d("WebView", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return false;
            }
        });

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient());  // Certifique-se de que o WebView não abra o conteúdo no navegador
//        webView.loadUrl("http://" + deviceIp + ":8080");
        webView.loadUrl("http://127.0.0.1:8080"); // Carregue o URL do servidor Go que você configurou
//        webView.loadUrl("http://10.0.2.2:8080");
        setContentView(webView);
    }

    private void startGoServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                app.App.app();
            }
        }).start();
    }

    // Método para obter o IP do dispositivo
    private String getDeviceIp() {
        // Usando WifiManager para pegar o IP local
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        return Formatter.formatIpAddress(ipAddress);
    }

    // Método para testar a conexão com o servidor
    private void testServerConnection() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL("http://10.0.2.2:8080");  // URL do servidor Go
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");  // Método da requisição (GET)
                    connection.setConnectTimeout(5000);  // Timeout de conexão em ms
                    connection.setReadTimeout(5000);  // Timeout de leitura em ms

                    int responseCode = connection.getResponseCode();  // Resposta HTTP
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();  // Retorna a resposta do servidor
                    } else {
                        return "Erro na conexão: " + responseCode;  // Retorna o código de erro
                    }
                } catch (Exception e) {
                    return "Erro na requisição: " + e.getMessage();  // Captura o erro
                }
            }

            @Override
            protected void onPostExecute(String result) {
                // Exibe o resultado da requisição (na tela ou no log)
                Log.d(TAG, "Resposta do servidor: " + result);
            }
        }.execute();
    }

}
