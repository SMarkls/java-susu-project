
package com.example.assistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.OkHttpClient;

public class Parser {
    private String login;
    private String password;
    private static OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new MyCookieJar())
            .build();

     public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
         return this.password;
    }

    public String getLogin() {
         return this.login;
    }
    public void configureAuthTokens() throws IOException {
        String verifyToken = fetchVerifyToken();
        if (verifyToken == null || verifyToken.isEmpty()) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        sendPostRequest("https://studlk.susu.ru/Account/Login", String.format("__RequestVerificationToken=%s&UserName=%s&dxPassword=%s", verifyToken, this.login, this.password));
    }

    private String fetchVerifyToken() throws IOException {
        String urlString = "https://studlk.susu.ru/Account/Login";
        String response = sendGetRequest(urlString);

        String tokenPattern = "name=\"__RequestVerificationToken\" type=\"hidden\" value=\"(.*)\" />";
        Pattern pattern = Pattern.compile(tokenPattern);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public List<ControlPoint> findAllControlPoints(String journalId) throws IOException {
        String urlString = "https://studlk.susu.ru/ru/StudyPlan/GetMarks";
        String postData = String.format("journalId=%s", journalId);

        String response = sendPostRequest(urlString, postData);

        List<ControlPoint> points = new ArrayList<>();
        String markPattern = "Name\":\"([а-яА-Я\\d\\w\\-(),. №]+)\",\"EventId\":\"[\\w\\d\\-]+\",\"TypeId\":\"[\\w\\d\\-]+\",\"StudentId\":\"[\\d\\w\\-]+\"" +
                             ",\"Fio\":\"[а-яА-Я\\w\\d ]+\",\"Point\":([\\w\\d\\.]+),\"Rating\":([\\w\\d\\.]+),\"IsMoodle\":[\\w]*,\"EventOrderNumber\":([\\d]*)";
        Pattern pattern = Pattern.compile(markPattern);
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            ControlPoint point = new ControlPoint();
            point.setName(matcher.group(1));
            point.setMark(matcher.group(2));
            point.setRating(matcher.group(3));
            point.setOrderNumber(Integer.parseInt(matcher.group(4)));
            points.add(point);
        }

        return points;
    }

    public List<Discipline> findAllDisciplines(String semestr) throws IOException {
        String urlString = "https://studlk.susu.ru/ru/StudyPlan/StudyPlanGridPartialCustom";
        List<Discipline> disciplines = new ArrayList<>();
        String response = sendGetRequest(urlString);

        String disciplinePattern = ",\"JournalId\":\"([\\w\\d\\-]*)\",\"TermNumber\":" + semestr + ",\"CycleName\":\"[\\d\\w]+\",\"DisciplineName\":\"([а-яА-Я\\- ]*)\"";
        Pattern pattern = Pattern.compile(disciplinePattern);
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            if (matcher.group(1).equals("00000000-0000-0000-0000-000000000000")) {
                continue;
            }

            Discipline discipline = new Discipline();
            discipline.setJournalId(matcher.group(1));
            discipline.setName(matcher.group(2));
            discipline.setSemestr(semestr);
            disciplines.add(discipline);
        }

        return disciplines;
    }

    private String sendGetRequest(String urlString) {
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("GET-запрос вернул код: " + response.code());
                return null;
            }

            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sendPostRequest(String urlString, String postData) {
        MediaType formUrlEncoded
                = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");
        RequestBody body = RequestBody.create(postData, formUrlEncoded);
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("POST-запрос вернул код: " + response.code());
                return null;
            }

            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }
}

