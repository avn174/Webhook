package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static HttpServer server;
    private static MyBot myBot;

    public static void main(String[] args) throws IOException {
        int port = 8081; // Порт, на котором будет запущен сервер
        String botToken = "5251863172:AAF5iEAPaXNO46BiRAcjePajCNWCqRgaveI"; // Замените на токен вашего бота
        String chatId = "-529016297"; // Замените на ваш Chat ID

        // Инициализация Telegram бота
        myBot = new MyBot(botToken, chatId);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/webhook", new WebhookHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Сервер запущен на порту " + port);

        // Добавляем обработчик для завершения программы по нажатию клавиши Enter
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Нажмите Enter для остановки сервера");
        reader.readLine();

        stopServer();
    }

    private static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Сервер остановлен");
        }
    }

    static class WebhookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();

            if (requestMethod.equalsIgnoreCase("GET")) {
                URI uri = exchange.getRequestURI();
                String query = uri.getQuery();
                String[] queryParams = query.split("&");

                List<String> messageParams = new ArrayList<>();
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];

                        // Заменяем соответствующие ключевые слова
                        if (key.equals("order")) {
                            key = "Заказ";
                        } else if (key.equals("date")) {
                            key = "Дата";
                        } else if (key.equals("time")) {
                            key = "Время";
                        } else if (key.equals("address")) {
                            key = "Адрес";
                        } else if (key.equals("place")) {
                            key = "Квартира";
                        } else if (key.equals("name")) {
                            key = "Получатель";
                        } else if (key.equals("phone")) {
                            key = "Телефон получателя";
                            // Заменяем первую цифру, если она 8 или 7, на "+7"
                            if (value.length() > 0 && (value.charAt(0) == '8' || value.charAt(0) == '7')) {
                                value = "+7" + value.substring(1);
                            }
                        } else if (key.equals("foto")) {
                            key = "Фото с получателем";
                        } else if (key.equals("cost")) {
                            key = "Стоимость доставки";
                        }

                        String paramMessage = key + ": " + value;
                        messageParams.add(paramMessage);
                    }
                }

                // Собираем все значения параметров в одну строку
                StringBuilder message = new StringBuilder();
                for (String paramMessage : messageParams) {
                    message.append(paramMessage).append("\n");
                }

                // Отправляем сообщение в Telegram
                myBot.sendMessage(message.toString());

                // Выводим строку с значениями параметров в консоль
                System.out.println(message.toString());
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        }
    }

    static class MyBot extends TelegramLongPollingBot {
        private final String botToken;
        private final String chatId;

        public MyBot(String botToken, String chatId) {
            this.botToken = botToken;
            this.chatId = chatId;
        }

        @Override
        public void onUpdateReceived(Update update) {
            // Метод не используется
        }

        @Override
        public String getBotUsername() {
            return "AndreyKurier_bot"; // Замените на имя вашего бота
        }

        @Override
        public String getBotToken() {
            return botToken;
        }

        public void sendMessage(String messageText) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
