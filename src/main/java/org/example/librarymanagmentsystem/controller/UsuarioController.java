package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.example.librarymanagmentsystem.daos.UsuarioDAO;
import org.example.librarymanagmentsystem.entidades.Usuario;
import org.example.librarymanagmentsystem.utils.FotoUtils;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UsuarioController implements Initializable {

    @FXML private TextField txtNome, txtUsuario, txtSenha;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtBuscar;
    @FXML private Circle fotoPerfil;
    @FXML private TableView<Usuario> tabelaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNome, colUsuario, colTipo, colStatus;
    @FXML private Button btnSalvar, btnAtualizar, btnDeletar, btnLimpar, btnSelecionarFoto;

    private UsuarioDAO usuarioDAO;
    private ObservableList<Usuario> usuariosList;
    private Usuario usuarioSelecionado;
    private String caminhoFotoSelecionada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioDAO = new UsuarioDAO();
        usuariosList = FXCollections.observableArrayList();

        configurarTabela();
        configurarCombos();
        configurarEventos();
        carregarDados();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isAtivo() ? "Ativo" : "Inativo"
                )
        );

        tabelaUsuarios.setItems(usuariosList);

        tabelaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                usuarioSelecionado = novo;
                preencherCampos(novo);
                carregarFotoPerfil(novo.getFoto());
            }
        });
    }

    private void configurarCombos() {
        cbTipo.setItems(FXCollections.observableArrayList("Administrador", "Funcionario"));
        cbTipo.setValue("Funcionario");
    }

    private void configurarEventos() {
        txtBuscar.textProperty().addListener((obs, old, novo) -> {
            if (novo.isEmpty()) {
                carregarDados();
            } else {
                buscarUsuarios();
            }
        });

        btnSalvar.setOnAction(e -> salvarUsuario());
        btnAtualizar.setOnAction(e -> atualizarUsuario());
        btnDeletar.setOnAction(e -> deletarUsuario());
        btnLimpar.setOnAction(e -> limparCampos());
        btnSelecionarFoto.setOnAction(e -> selecionarFoto());
    }

    public void carregarDados() {
        new Thread(() -> {
            try {
                List<Usuario> usuarios = usuarioDAO.listarTodos();
                Platform.runLater(() -> {
                    usuariosList.clear();
                    usuariosList.addAll(usuarios);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao carregar usuários: " + e.getMessage()));
            }
        }).start();
    }

    private void salvarUsuario() {
        if (!validarCampos()) return;

        try {
            Usuario usuario = new Usuario();
            usuario.setNome(txtNome.getText().trim());
            usuario.setUsuario(txtUsuario.getText().trim());
            usuario.setSenha(txtSenha.getText().trim());
            usuario.setTipo(cbTipo.getValue());
            usuario.setFoto(caminhoFotoSelecionada);
            usuario.setAtivo(true);

            usuarioDAO.inserir(usuario);
            mostrarSucesso("Usuário cadastrado com sucesso!");
            limparCampos();
            carregarDados();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarErro("Nome de usuário já existe!");
            } else {
                mostrarErro("Erro ao salvar: " + e.getMessage());
            }
        }
    }

    private void atualizarUsuario() {
        if (usuarioSelecionado == null) {
            mostrarAviso("Selecione um usuário para atualizar!");
            return;
        }

        if (!validarCampos()) return;

        try {
            usuarioSelecionado.setNome(txtNome.getText().trim());
            usuarioSelecionado.setUsuario(txtUsuario.getText().trim());

            // Só atualizar senha se foi digitada uma nova
            if (!txtSenha.getText().trim().isEmpty()) {
                usuarioSelecionado.setSenha(txtSenha.getText().trim());
            }

            usuarioSelecionado.setTipo(cbTipo.getValue());
            if (caminhoFotoSelecionada != null) {
                usuarioSelecionado.setFoto(caminhoFotoSelecionada);
            }

            usuarioDAO.atualizar(usuarioSelecionado);
            mostrarSucesso("Usuário atualizado com sucesso!");
            limparCampos();
            carregarDados();

        } catch (SQLException e) {
            mostrarErro("Erro ao atualizar: " + e.getMessage());
        }
    }

    private void deletarUsuario() {
        if (usuarioSelecionado == null) {
            mostrarAviso("Selecione um usuário para deletar!");
            return;
        }

        // Não permitir deletar o próprio usuário logado
        if (usuarioSelecionado.getIdUsuario() == getUsuarioLogadoId()) {
            mostrarErro("Você não pode deletar seu próprio usuário!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja deletar o usuário " +
                usuarioSelecionado.getNome() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                usuarioDAO.deletar(usuarioSelecionado.getIdUsuario());
                mostrarSucesso("Usuário deletado com sucesso!");
                limparCampos();
                carregarDados();
            } catch (SQLException e) {
                mostrarErro("Erro ao deletar: " + e.getMessage());
            }
        }
    }

    private void buscarUsuarios() {
        String busca = txtBuscar.getText().trim();
        if (busca.isEmpty()) {
            carregarDados();
            return;
        }

        new Thread(() -> {
            try {
                List<Usuario> resultados = usuarioDAO.listarTodos();
                List<Usuario> filtrados = resultados.stream()
                        .filter(u -> u.getNome().toLowerCase().contains(busca.toLowerCase()) ||
                                u.getUsuario().toLowerCase().contains(busca.toLowerCase()))
                        .toList();

                Platform.runLater(() -> {
                    usuariosList.clear();
                    usuariosList.addAll(filtrados);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void selecionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File arquivo = fileChooser.showOpenDialog(null);
        if (arquivo != null) {
            caminhoFotoSelecionada = arquivo.getAbsolutePath();
            carregarFotoPerfil(caminhoFotoSelecionada);
        }
    }

    private void carregarFotoPerfil(String caminhoFoto) {
        FotoUtils.carregarFoto(fotoPerfil, caminhoFoto);
    }

    private void preencherCampos(Usuario u) {
        txtNome.setText(u.getNome());
        txtUsuario.setText(u.getUsuario());
        txtSenha.clear(); // Não mostrar senha
        cbTipo.setValue(u.getTipo());
        caminhoFotoSelecionada = u.getFoto();
    }

    public void limparCampos() {
        txtNome.clear();
        txtUsuario.clear();
        txtSenha.clear();
        cbTipo.setValue("Funcionario");
        caminhoFotoSelecionada = null;
        usuarioSelecionado = null;
        carregarFotoPerfil(null);
        tabelaUsuarios.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o nome do usuário!");
            txtNome.requestFocus();
            return false;
        }
        if (txtUsuario.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o nome de usuário!");
            txtUsuario.requestFocus();
            return false;
        }
        if (usuarioSelecionado == null && txtSenha.getText().trim().isEmpty()) {
            mostrarAviso("Preencha a senha!");
            txtSenha.requestFocus();
            return false;
        }
        return true;
    }

    private int getUsuarioLogadoId() {
        return 1;
    }

    private void mostrarSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarAviso(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void inicializar(){

    }
}