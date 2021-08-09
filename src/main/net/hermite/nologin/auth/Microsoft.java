package net.hermite.nologin.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.hermite.nologin.account.Account;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Microsoft {
    private static final String authTokenUrl = "https://login.live.com/oauth20_token.srf";
    private static final String xblAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String xstsAuthUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String mcLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String mcStoreUrl = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String mcProfileUrl = "https://api.minecraftservices.com/minecraft/profile";

    public static Account auth(String authCode) {

        JsonObject accessTokenJson = getAccessToken(authCode);

        JsonObject xblAuthJson = xblAuth(accessTokenJson.get("access_token").getAsString());
        JsonObject xstsJson = acquireXsts(xblAuthJson.get("Token").getAsString());

        String uhs = xstsJson.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
        JsonObject minecraftJson = acquireMinecraftToken(uhs, xstsJson.get("Token").getAsString());

        if (ownGame(minecraftJson.get("access_token").getAsString())) {
            JsonObject mcProfileJson = getMcProfile(minecraftJson.get("access_token").getAsString());

            return new Account(mcProfileJson.get("id").getAsString(), mcProfileJson.get("name").getAsString(), minecraftJson.get("access_token").getAsString(),
                    mcProfileJson.get("id").getAsString(), mcProfileJson.get("name").getAsString(), true, accessTokenJson.get("refresh_token").getAsString());

        }

        return null;
    }

    public static Account refreshToken(Account account) {
        JsonObject accessTokenJson = getAccessToken(account.getRefreshToken(), "refresh_token");

        JsonObject xblAuthJson = xblAuth(accessTokenJson.get("access_token").getAsString());
        JsonObject xstsJson = acquireXsts(xblAuthJson.get("Token").getAsString());

        String uhs = xstsJson.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
        JsonObject minecraftJson = acquireMinecraftToken(uhs, xstsJson.get("Token").getAsString());

        account.setRefreshToken(accessTokenJson.get("refresh_token").getAsString());
        account.setAccessToken(minecraftJson.get("access_token").getAsString());

        return account;
    }

    public static JsonObject getAccessToken(String code) {
        return getAccessToken(code, "authorization_code");
    }

    private static JsonObject getAccessToken(String code, String type) {
        try {

            String formData = "";
            if (type.equals("authorization_code")) {
                formData = "client_id=00000000402b5328&code=" + code +
                        "&grant_type="+type+"&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL";
            } else {
                formData = "client_id=00000000402b5328&refresh_token=" + code +
                        "&grant_type="+type+"&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL";
            }

            URL uri = new URL(authTokenUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(formData.getBytes(StandardCharsets.UTF_8).length));
            con.setDoInput(true);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(formData.getBytes(StandardCharsets.UTF_8));
            wr.close();

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            System.out.println(response);
            return new Gson().fromJson(response, JsonObject.class);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JsonObject xblAuth(String token) {
        try {
            String payload = "{\n" +
                    "    \"Properties\": {\n" +
                    "        \"AuthMethod\": \"RPS\",\n" +
                    "        \"SiteName\": \"user.auth.xboxlive.com\",\n" +
                    "        \"RpsTicket\": \""+token+"\"\n" +
                    "    },\n" +
                    "    \"RelyingParty\": \"http://auth.xboxlive.com\",\n" +
                    "    \"TokenType\": \"JWT\"\n" +
                    " }";

            URL uri = new URL(xblAuthUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(payload.getBytes(StandardCharsets.UTF_8).length));
            con.setDoInput(true);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(payload.getBytes(StandardCharsets.UTF_8));

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }



            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            return new Gson().fromJson(response, JsonObject.class);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JsonObject acquireXsts(String xblToken) {
        try {
            String payload = "{\n" +
                    "    \"Properties\": {\n" +
                    "        \"SandboxId\": \"RETAIL\",\n" +
                    "        \"UserTokens\": [\""+xblToken+"\"]\n" +
                    "    },\n" +
                    "    \"RelyingParty\": \"rp://api.minecraftservices.com/\",\n" +
                    "    \"TokenType\": \"JWT\"\n" +
                    " }";

            URL uri = new URL(xstsAuthUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(payload.getBytes(StandardCharsets.UTF_8).length));
            con.setDoInput(true);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(payload.getBytes(StandardCharsets.UTF_8));

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            return new Gson().fromJson(response, JsonObject.class);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JsonObject acquireMinecraftToken(String xblUhs, String xblXsts) {
        try {
            String payload = "{\n" +
                    "\"identityToken\": \"XBL3.0 x=" + xblUhs + ";" + xblXsts + "\"" +
                    " }";

            URL uri = new URL(mcLoginUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(payload.getBytes(StandardCharsets.UTF_8).length));
            con.setDoInput(true);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(payload.getBytes(StandardCharsets.UTF_8));

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            System.out.println(response);
            return new Gson().fromJson(response, JsonObject.class);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean ownGame(String mcAccessToken) {
        try {
            URL uri = new URL(mcStoreUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            con.setDoOutput(false);
            con.setDoInput(true);

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);

            if (responseJson.get("items").getAsJsonArray().size() > 0) return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private static JsonObject getMcProfile(String mcAccessToken) {
        try {
            URL uri = new URL(mcProfileUrl);
            HttpURLConnection con = (HttpURLConnection) uri.openConnection();
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            con.setDoOutput(false);
            con.setDoInput(true);

            BufferedReader br;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String response = "";
            for (String line; (line = br.readLine()) != null; response += line);

            return new Gson().fromJson(response, JsonObject.class);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
