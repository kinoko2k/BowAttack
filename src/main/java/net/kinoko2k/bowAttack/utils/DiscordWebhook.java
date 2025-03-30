package net.kinoko2k.bowAttack.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DiscordWebhook {
    private final String url;
    private String username;
    private String avatarUrl;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {
        if (embeds.isEmpty()) return;

        URL webhookUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) webhookUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        String jsonPayload = buildJson();
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes());
        }

        connection.getInputStream().close();
    }

    private String buildJson() {
        StringBuilder json = new StringBuilder("{");

        if (username != null) json.append("\"username\":\"").append(username).append("\",");
        if (avatarUrl != null) json.append("\"avatar_url\":\"").append(avatarUrl).append("\",");

        json.append("\"embeds\":[");
        for (EmbedObject embed : embeds) {
            json.append(embed.toJson()).append(",");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("]}");

        return json.toString();
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private int color;

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setColor(int color) {
            this.color = color;
            return this;
        }

        public String toJson() {
            return String.format("{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d}", title, description, color);
        }
    }
}