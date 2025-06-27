package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class Result {

    public static String findLongestDurationMission(String firstAgency, String secondAgency) {
        String baseUrl = "https://ll.thespacedevs.com/2.2.0/launch/";
        int totalMissionsCount = 0;

        //armazena melhor resultado da primeira agencia
        int maxDurationFirstAgency = -1;
        String missionNameFirstAgency = "";
        boolean foundInFirstAgency = false; // FLAG CRUCIAL: true se encontrarmos QUALQUER missao na firstAgency

        //melhor resultado da segunda agencia
        int maxDurationSecondAgency = -1;
        String missionNameSecondAgency = "";

        try {
           //count total de missoes
            String initialUrl = baseUrl + "?limit=1";
          //  System.out.println("DEBUG: Fazendo requisição inicial para: " + initialUrl);
            String initialResponse = getApiResponse(initialUrl);
            JSONObject initialJson = new JSONObject(initialResponse);
            totalMissionsCount = initialJson.getInt("count");
          //  System.out.println("DEBUG: Total de missões disponíveis: " + totalMissionsCount);


            int limit = 100; //num de restultado por pagina

            //PAGINACAO
            //loop principal de paginacao iterando por tds pags
            for (int offset = 0; offset < totalMissionsCount; offset += limit) {
                String pageUrl = baseUrl + "?limit=" + limit + "&offset=" + offset;
           //     System.out.println("DEBUG: Buscando URL de página: " + pageUrl);

                String pageResponse = getApiResponse(pageUrl);
                JSONObject pageJson = new JSONObject(pageResponse);
                JSONArray results = pageJson.getJSONArray("results");

                //loop interno pra iterar sob cada missao da pag atual
                for (int i = 0; i < results.length(); i++) {
                    JSONObject mission = results.getJSONObject(i);

                    //extraindo dados da missao
                    String currentMissionName = "";
                    String currentAgencyName = "";
                    int currentDurationInDays = 0;

                    //nome da missao
                    if (mission.has("name") && !mission.isNull("name")) {
                        currentMissionName = mission.getString("name");
                    }

                    //nome da agencia
                    if (mission.has("launch_service_provider") && !mission.isNull("launch_service_provider")) {
                        JSONObject lsp = mission.getJSONObject("launch_service_provider");
                        if (lsp.has("name") && !lsp.isNull("name")) {
                            currentAgencyName = lsp.getString("name");
                        }

                    }

                    //duraçao em dias add tratamento p nulos e ausentes
                    if (mission.has("mission") && !mission.isNull("mission")) {
                        JSONObject missionDetail = mission.getJSONObject("mission");
                        if (missionDetail.has("duration_in_days") && !missionDetail.isNull("duration_in_days")) {
                            currentDurationInDays = missionDetail.getInt("duration_in_days");
                        }
                    }
                    //debug
                    // System.out.println("  DEBUG: Processando missão -> Nome: '" + currentMissionName +
                    //                    "', Agência: '" + currentAgencyName +
                    //                    "', Duração: " + currentDurationInDays + " dias.");

                    //comparando: a missao pertence a 1a agencia? (usar ignroecase)
                    if (currentAgencyName.equalsIgnoreCase(firstAgency)) {
                        //encontrou missao para a primeira agencia, marca a flag, se a flag for vdd significa q n precisaremos do result da segunda agencia
                        foundInFirstAgency = true;

                        //duraçao dessa missao > q a encontrada p 1a agencia, atualiza melhor resultado
                        if (currentDurationInDays > maxDurationFirstAgency) {
                            maxDurationFirstAgency = currentDurationInDays;
                            missionNameFirstAgency = currentMissionName;
                            // System.out.println("DEBUG: Nova melhor para " + firstAgency + ": " + missionNameFirstAgency + " (" + maxDurationFirstAgency + " dias)");
                        }
                    }
                    //se n for da 1a verificar se pertence 2a agencia
                    //else if evita q missao seja contada p ambas agencias, agencias c nomes ligeiramente diferentes
                    else if (currentAgencyName.equalsIgnoreCase(secondAgency)) {
                        //duracao > q o maxAtual p 2a agencia ? -> entao atualiza melhor resultado
                        //basear decisao final APENAS em foundinfirstagency''
                        if (currentDurationInDays > maxDurationSecondAgency) {
                            maxDurationSecondAgency = currentDurationInDays;
                            missionNameSecondAgency = currentMissionName;

                        }
                    }
                }
            }



            //percorreu tds paginas e processou tds missoes -> decidimos qual missao retornar
            if (foundInFirstAgency) {
                //encontrou ao menos 1 missao p 1a agencia? retornamos a melhor missao encontrada para ela.
              //  System.out.println("DEBUG: Encontrado resultado para " + firstAgency + ". Retornando: " + missionNameFirstAgency);
                return missionNameFirstAgency;
            } else {
                //se n encontra nenhuma p 1a agencia -> retorna a melhor missao encontrada p 2a agencia
                //garantir que SEMPRE ENCONTRARA em uma das duas
               // System.out.println("DEBUG: Nenhum resultado para " + firstAgency + ". Retornando fallback para " + secondAgency + ": " + missionNameSecondAgency);
                return missionNameSecondAgency;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERRO INESPERADO: " + e.getMessage();
        }
    }

    //metodo getapi aqui
    //private static String getApiResponse
    private static String getApiResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();
            return response;
        } else {
            try (BufferedReader err = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String errorResponse = err.lines().collect(Collectors.joining());
              //  System.err.println("DEBUG: Resposta de erro da API: " + errorResponse);
            }
            throw new RuntimeException("Requisição HTTP GET falhou com código de erro: " + responseCode);
        }
    }
}