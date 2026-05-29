package org.example.infrastructure.database;

import java.io.*;
import java.util.Properties;


import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ConfigConnection {
    private static final String CONFIG_FILE = "config.properties";
    private static final String DB_KEY = "database.path";



    // Retorna o caminho do banco. Se não existir, força a escolha.
    public static String getDatabasePath(Stage stage) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
                String path = props.getProperty(DB_KEY);
                if (path != null && !path.trim().isEmpty()) {
                    return path;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Se chegou aqui, é a primeira vez rodando ou o arquivo sumiu. Força a escolha.
        return selecionarENovarDiretorio(stage, props);
    }

    private static String selecionarENovarDiretorio(Stage stage, Properties props) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta para salvar o Banco de Dados");

        // Define uma pasta inicial padrão (Documents) para facilitar
        String userHome = System.getProperty("user.home");
        directoryChooser.setInitialDirectory(new File(userHome + File.separator + "Documents"));

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory == null) {
            // Se o usuário fechar sem escolher, cria uma pasta padrão para o app não quebrar
            File padrao = new File(System.getProperty("user.home") + File.separator + "MinhasFinancasPadrao");
            padrao.mkdirs();
            selectedDirectory = padrao;
        }

        String caminhoFinal = selectedDirectory.getAbsolutePath() + File.separator + "financas.db";

        // Salva a escolha no arquivo de propriedades para os próximos acessos
        props.setProperty(DB_KEY, caminhoFinal);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Configuracoes do Sistema Financeiro");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return caminhoFinal;
    }
}