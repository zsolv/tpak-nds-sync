package ch.zsolv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tpak API class
 */
public class Tpak {
    
    // Credentials to access tpak
    private final String username;
    private final String password;
    private String token;

    private static Tpak instance = null;

    private List<Athlete> athletes;

    private Tpak(String username, String password){
        this.username = username;
        this.password = password;
    }

    public static boolean login(String username, String password){
        if (instance != null ){
            return false;
        }
        Tpak t = new Tpak(username, password);
        if (t.doLogin()){
            instance = t;
            return true;
        }
        return false;
    }
    public static Tpak getInstance(){
        return instance;
    }


    public List<Athlete> athletesWithoutPermission(){
        List<Athlete> allAthletes = getAllAthletes();
        List<Athlete> noAccess = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        for (Athlete athlete: allAthletes){
            if(doGet("https://www.t-pak.ch/api/activities/"+today+"/"+today+"?user="+athlete.id) == null){
                noAccess.add(athlete);
            }
        }
        if(noAccess.size() == 0){
            noAccess.add(new Athlete("", "You have access to everyone :)", 0, 0,"", "", false));
        }
        return noAccess;
    }


    /**
     * Returns true if a given athlete has trained on a certain date
     * @param athlete : athlete object to check
     * @param date : at witch date to check
     * @return
     */
    public boolean athleteHasRegioTrainingAtDate(Athlete athlete, Date date){

        // Get date format for api request
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

        if(athlete.skio){
            int month = Integer.parseInt(new SimpleDateFormat("MM").format(date));
            int day = Integer.parseInt(new SimpleDateFormat("dd").format(date));
            if (month > 10 || month < 3 || (month == 10 && day >= 3) || (month == 3 && day <= 24)){
                return false;
            }
        }

        // Request api
        String jsonBody = doGet("https://www.t-pak.ch/api/activities/"+formattedDate+"/"+formattedDate+"?user="+athlete.id);

        // return false if athlete has not given you access
        if (jsonBody == null){
            return false;
        }

        List<Integer> ignoreIds = Stream.of(4,6,13,40,41,73,74,75,76,77,78,79,80,123,125,126,192,193,198,199,200,217,218,200,221,222,223,224,225,381,388,613).toList();


        // Parese response as json array
        JSONArray response = new JSONArray(jsonBody);
        int timeCountToday = 0;

        // go threw activities
        for(int i=0; i<response.length(); ++i){
            // Check if activity is assigned to regiokader --> activityParamId 299 (trainingn assignment) is 329 (regio kader)
            boolean isRegio = false;
            JSONArray params =  response.getJSONObject(i).getJSONArray("parameters");
            for(int j=0; j<params.length(); ++j){
                if (params.getJSONObject(j).getInt("activityParameterId") == 299 && 
                    params.getJSONObject(j).getString("value").equals("329")){
                    isRegio = true;
                    break;
                }
            }
            // if not regiokader, ignore
            if (!isRegio){
                continue;
            }
            // if activity to ignore such as "Wellness", ignore
            if (ignoreIds.contains(response.getJSONObject(i).getInt("activityTypeId"))){
                continue;
            }

            // go threw subactivities and if not ignored, count time
            JSONArray subactivities = response.getJSONObject(i).getJSONArray("subActivities");
            for(int j=0; j<subactivities.length(); ++j){
                if (ignoreIds.contains(subactivities.getJSONObject(j).getInt("subActivityTypeId"))){
                    continue;
                }
                timeCountToday += subactivities.getJSONObject(j).getInt("duration");
            }

            if (timeCountToday >= 10){
                return true;
            }

        }
        return false;
        
    }

    /**
     * Returns a list of all athletes in your team
     * @return
     */
    public List<Athlete> getAllAthletes(){
        if(athletes != null){
            return athletes;
        }
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        String skiFrom = "";
        String skiTo = "";
        if (month >= 10){
            skiFrom = year+"-10-03";
            skiTo = (year+1)+"-03-24";
        }else{
            skiFrom = (year-1)+"-10-03";
            skiTo = year+"-03-24";
        }
    
        ArrayList<Athlete> athleteIds = new ArrayList<>();

        JSONObject jsonRelations = new JSONObject(doGet("https://www.t-pak.ch/api/users/relations"));
        JSONArray teams = jsonRelations.getJSONObject("roles").getJSONArray("coach");
        for (int i = 0; i<teams.length(); ++i) {
            
            JSONArray response = new JSONArray(doGet("https://www.t-pak.ch/api/teams/"+teams.getJSONObject(i).getInt("id")+"/athletes"));
            for(int j=0; j<response.length(); ++j){

                String skiActivity = doGet("https://www.t-pak.ch/api/activities/"+skiFrom+"/"+skiTo+"?user="+response.getJSONObject(j).getInt("id"));
                boolean skio = false;
                skio:
                if (skiActivity != null){
                    JSONArray skiActivities = new JSONArray(skiActivity);
                    for(int k=0; k<skiActivities.length(); ++k){
                        JSONArray params =  skiActivities.getJSONObject(k).getJSONArray("parameters");
                        for(int l=0; l<params.length(); ++l){
                            if (params.getJSONObject(l).getInt("activityParameterId") == 299 && 
                                params.getJSONObject(l).getString("value").equals("331")){
                                    skio = true;
                                break skio;
                            }
                        }
                    }
                }
                athleteIds.add(new Athlete(
                    response.getJSONObject(j).getString("email"),
                    response.getJSONObject(j).getString("firstname"),
                    response.getJSONObject(j).getInt("gender"),
                    response.getJSONObject(j).getInt("id"),
                    response.getJSONObject(j).getString("lastname"),
                    response.getJSONObject(j).getString("nickname"),
                    skio
                ));
            }

        }
        
        athletes = athleteIds.stream().distinct().collect(Collectors.toList());
        return athletes;
    }

    /**
     * Checks wether your token is still a valid session and login else
     */
    private void getValidSession(){
        if (token == null){
            doLogin();
        }
        if (!checkIfValidSession()){
            doLogin();
        }
    }

    /**
     * returns true if we have a valid session
     */
    private boolean checkIfValidSession(){
        
        try {
            URL url = new URL("https://www.t-pak.ch/api/authenticate/check-session");
            HttpsURLConnection con;
            con = (HttpsURLConnection) url.openConnection();
         
            con.setRequestMethod("GET");
            con.setRequestProperty("x-auth-token", token);
            return con.getResponseCode() == 204;
            
        } catch (MalformedURLException e) {
            System.out.println("Tpak: validate Session --> invalid URL");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Tpak: validate Session --> io exception");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkCredentials(String username, String password){
        Tpak t = new Tpak(username, password);
        return t.doLogin() && t.checkIfValidSession();
    }

    /**
     * Perform login to get an access token usable for the api
     */
    private boolean doLogin(){
        final String body = "{\"username\":\""+username+"\",\"password\":\""+password+"\",\"longSession\":true}";
        try {
            URL url = new URL("https://www.t-pak.ch/api/authenticate/login");
     
            HttpsURLConnection con;
            con = (HttpsURLConnection) url.openConnection();
         
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json, text/plain, */*");
            con.setRequestProperty("Content-type", "application/json; charset=utf-8");

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();
            
            if (con.getResponseCode() != 200) {
                return false;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();
            JSONObject jsonObject = new JSONObject(response.toString());
            if (!jsonObject.getBoolean("success")){
                return false;
            } else {}
            
            this.token = jsonObject.getString("token");
            
        } catch (MalformedURLException e) {
            System.out.println("Tpak: login --> invalid URL");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Tpak: login --> ioexception");
            e.printStackTrace();
        }
        return true;
    }
    
    /**
     * Do a get request to the api
     * @param url
     * @return
     */
    private String doGet(String url){
        try {
            // Check for a valid session
            getValidSession();
            
            // Create connection and set Header
            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("x-auth-token", token);
            con.setRequestProperty("Accept", "application/json, text/plain, */*");
        
            // if not status 200, return null
            if (con.getResponseCode() != 200) {
                return null;
            }
            
            // Read response and return as string
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            return response.toString();
            
        } catch (MalformedURLException e) {
            System.out.println("Tpak: api request --> invalid URL");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Tpak: api request --> io exception");
            e.printStackTrace();
        }
        return null;
    }

}
