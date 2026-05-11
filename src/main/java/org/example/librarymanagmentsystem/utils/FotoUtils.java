package org.example.librarymanagmentsystem.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.example.librarymanagmentsystem.entidades.Usuario;

import java.io.File;
import java.net.URL;

public class FotoUtils {

    private static final String DEFAULT_FOTO_PATH = "/imagens/default-user.png";

    /**
     * Carrega a foto do usuário no Circle
     */
    public static void carregarFoto(Circle circle, String caminhoFoto) {
        if (circle == null) return;

        if (caminhoFoto != null && !caminhoFoto.isEmpty()) {
            Image image = carregarImagem(caminhoFoto);
            if (image != null && !image.isError()) {
                circle.setFill(new ImagePattern(image));
                return;
            }
        }

        // Fallback: imagem padrão
        usarImagemPadrao(circle);
    }

    /**
     * Carrega a foto do usuário no Circle com fallback para iniciais
     */
    public static void carregarFotoComFallback(Circle circle, Usuario usuario) {
        if (circle == null || usuario == null) return;

        if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            Image image = carregarImagem(usuario.getFoto());
            if (image != null && !image.isError()) {
                circle.setFill(new ImagePattern(image));
                return;
            }
        }

        // Fallback: usar iniciais
        usarIniciais(circle, usuario.getIniciais());
    }

    /**
     * Carrega imagem de diferentes fontes
     */
    private static Image carregarImagem(String caminho) {
        try {
            // 1. Tenta como recurso do classpath
            URL resourceUrl = FotoUtils.class.getResource(caminho);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm());
            }

            // 2. Tenta como arquivo do sistema
            File file = new File(caminho);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // 3. Tenta como caminho relativo ao projeto
            String projectPath = System.getProperty("user.dir");
            File projectFile = new File(projectPath + caminho);
            if (projectFile.exists()) {
                return new Image(projectFile.toURI().toString());
            }

            // 4. Tenta como recurso com barra
            resourceUrl = FotoUtils.class.getResource("/" + caminho);
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm());
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
        }

        return null;
    }

    /**
     * Usa imagem padrão
     */
    private static void usarImagemPadrao(Circle circle) {
        try {
            URL defaultUrl = FotoUtils.class.getResource(DEFAULT_FOTO_PATH);
            if (defaultUrl != null) {
                Image defaultImage = new Image(defaultUrl.toExternalForm());
                circle.setFill(new ImagePattern(defaultImage));
            } else {
                // Fallback: cor sólida
                circle.setFill(Color.web("#1b5e90"));
            }
        } catch (Exception e) {
            circle.setFill(Color.web("#1b5e90"));
        }
    }

    /**
     * Usa iniciais como fallback (quando não tem imagem)
     */
    private static void usarIniciais(Circle circle, String iniciais) {
        circle.setFill(Color.web("#1b5e90"));
        // Nota: Para colocar texto dentro do Circle, seria necessário usar um Group
        // Esta é uma implementação simplificada
    }

    /**
     * Salva foto (copia para o diretório do projeto)
     */
    public static String salvarFoto(File arquivoOrigem, String nomeUsuario) {
        try {
            String extensao = getExtensao(arquivoOrigem.getName());
            String nomeDestino = nomeUsuario + "_" + System.currentTimeMillis() + "." + extensao;
            String caminhoDestino = "src/main/resources/imagens/usuarios/" + nomeDestino;

            File destino = new File(caminhoDestino);
            destino.getParentFile().mkdirs();

            java.nio.file.Files.copy(arquivoOrigem.toPath(), destino.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/imagens/usuarios/" + nomeDestino;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtém extensão do arquivo
     */
    private static String getExtensao(String nomeArquivo) {
        int lastDot = nomeArquivo.lastIndexOf('.');
        if (lastDot > 0) {
            return nomeArquivo.substring(lastDot + 1);
        }
        return "jpg";
    }
}