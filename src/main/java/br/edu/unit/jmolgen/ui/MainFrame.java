package br.edu.unit.jmolgen.ui;

import br.edu.unit.jmolgen.generation.GenerationManifest;
import br.edu.unit.jmolgen.generation.GenerationPlan;
import br.edu.unit.jmolgen.generation.ProjectGenerator;
import br.edu.unit.jmolgen.json.ProjectSpecJsonCodec;
import br.edu.unit.jmolgen.model.LicenseType;
import br.edu.unit.jmolgen.model.ProjectSpec;
import br.edu.unit.jmolgen.process.MavenVerificationResult;
import br.edu.unit.jmolgen.process.MavenVerifier;
import br.edu.unit.jmolgen.template.TemplateException;
import br.edu.unit.jmolgen.validation.InvalidProjectSpecException;
import br.edu.unit.jmolgen.validation.ProjectSpecValidator;
import br.edu.unit.jmolgen.validation.ValidationError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Interface para preencher, visualizar, gerar e validar projetos. */
public final class MainFrame extends JFrame {

  private final ProjectGenerator generator = new ProjectGenerator();
  private final ProjectSpecJsonCodec codec = new ProjectSpecJsonCodec();
  private final MavenVerifier verifier = new MavenVerifier();
  private final Map<String, JTextField> fields = new LinkedHashMap<>();
  private final Border fieldBorder = UIManager.getBorder("TextField.border");
  private final JTextField baseDirectory = new JTextField(System.getProperty("user.home"), 32);
  private final JComboBox<String> javaVersion = new JComboBox<>(
      ProjectSpecValidator.SUPPORTED_JAVA_VERSIONS.toArray(String[]::new));
  private final JComboBox<LicenseType> license = new JComboBox<>(LicenseType.values());
  private final JTextArea preview = new JTextArea();
  private final JTextArea result = new JTextArea();
  private final JLabel status = new JLabel("Preencha os dados do projeto.");
  private final JProgressBar progress = new JProgressBar();
  private final JButton generateButton = new JButton("Gerar e validar");
  private final JTabbedPane tabs = new JTabbedPane();

  public MainFrame() {
    super("JMol Project Generator");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(760, 610));
    setSize(900, 700);
    setLocationByPlatform(true);

    tabs.addTab("Dados", createDataTab());
    tabs.addTab("Opcoes", createOptionsTab());
    tabs.addTab("Previa", textPanel(preview));
    tabs.addTab("Resultado", textPanel(result));
    add(tabs, BorderLayout.CENTER);
    add(createActions(), BorderLayout.SOUTH);
    installListeners();
    setStarterValues();
    refreshPreview();
  }

  private JPanel createDataTab() {
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    addField(form, "Nome do projeto", "projectName", "Analise Molecular");
    addField(form, "ArtifactId", "artifactId", "analise-molecular");
    addField(form, "Package Java", "basePackage", "br.edu.unit.quimica");
    addField(form, "Autor", "author", "Seu nome");
    addField(form, "Descricao", "description", "Descricao curta do projeto");
    addField(form, "Primeira classe cientifica", "firstClassName", "MolecularGeometry");

    GridBagConstraints constraints = rowConstraints(form.getComponentCount());
    form.add(new JLabel("Pasta base"), constraints);
    JPanel destination = new JPanel(new BorderLayout(8, 0));
    destination.add(baseDirectory, BorderLayout.CENTER);
    JButton browse = new JButton("Selecionar...");
    browse.addActionListener(event -> chooseBaseDirectory());
    destination.add(browse, BorderLayout.EAST);
    constraints.gridx = 1;
    constraints.weightx = 1;
    form.add(destination, constraints);
    constraints.gridy++;
    constraints.gridx = 0;
    constraints.gridwidth = 2;
    constraints.weighty = 1;
    form.add(new JLabel(), constraints);
    return form;
  }

  private JPanel createOptionsTab() {
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    addChoice(form, "Versao minima do Java", javaVersion, 0);
    addChoice(form, "Licenca", license, 1);
    GridBagConstraints constraints = rowConstraints(2);
    constraints.gridx = 0;
    constraints.gridwidth = 2;
    constraints.weighty = 1;
    form.add(new JLabel(), constraints);
    return form;
  }

  private void addField(JPanel form, String label, String name, String value) {
    GridBagConstraints constraints = rowConstraints(form.getComponentCount());
    form.add(new JLabel(label), constraints);
    JTextField field = new JTextField(value, 34);
    fields.put(name, field);
    constraints.gridx = 1;
    constraints.weightx = 1;
    form.add(field, constraints);
  }

  private static void addChoice(JPanel form, String label, Component choice, int row) {
    GridBagConstraints constraints = rowConstraints(row);
    form.add(new JLabel(label), constraints);
    constraints.gridx = 1;
    constraints.weightx = 1;
    form.add(choice, constraints);
  }

  private JPanel createActions() {
    JPanel actions = new JPanel(new BorderLayout(10, 8));
    actions.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
    JPanel buttons = new JPanel();
    JButton importButton = new JButton("Importar JSON");
    importButton.addActionListener(event -> importSpec());
    JButton exportButton = new JButton("Exportar JSON");
    exportButton.addActionListener(event -> exportSpec());
    generateButton.addActionListener(event -> generateProject());
    buttons.add(importButton);
    buttons.add(exportButton);
    buttons.add(generateButton);
    actions.add(buttons, BorderLayout.WEST);
    JPanel feedback = new JPanel(new BorderLayout(8, 0));
    feedback.add(status, BorderLayout.CENTER);
    progress.setIndeterminate(true);
    progress.setPreferredSize(new Dimension(100, 16));
    progress.setVisible(false);
    feedback.add(progress, BorderLayout.EAST);
    actions.add(feedback, BorderLayout.SOUTH);
    return actions;
  }

  private static JPanel textPanel(JTextArea area) {
    area.setEditable(false);
    area.setLineWrap(false);
    area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(area), BorderLayout.CENTER);
    return panel;
  }

  private static GridBagConstraints rowConstraints(int row) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = row;
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(7, 5, 7, 12);
    return constraints;
  }

  private void installListeners() {
    DocumentListener listener = new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        refreshPreview();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        refreshPreview();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        refreshPreview();
      }
    };
    fields.values().forEach(field -> field.getDocument().addDocumentListener(listener));
    javaVersion.addActionListener(event -> refreshPreview());
    license.addActionListener(event -> refreshPreview());
  }

  private void setStarterValues() {
    fields.get("projectName").setText("Projeto Cientifico");
    fields.get("artifactId").setText("projeto-cientifico");
    fields.get("basePackage").setText("br.edu.unit.ciencia");
    fields.get("author").setText(System.getProperty("user.name", "Autor"));
    fields.get("description").setText("Projeto cientifico gerado pelo JMol Project Generator");
    fields.get("firstClassName").setText("ScientificProject");
    javaVersion.setSelectedItem("21");
    license.setSelectedItem(LicenseType.MIT);
  }

  private ProjectSpec currentSpec() {
    return new ProjectSpec(fields.get("projectName").getText(),
        fields.get("artifactId").getText(), fields.get("basePackage").getText(),
        fields.get("author").getText(), fields.get("description").getText(),
        (String) javaVersion.getSelectedItem(), (LicenseType) license.getSelectedItem(),
        fields.get("firstClassName").getText());
  }

  private void refreshPreview() {
    clearFieldErrors();
    try {
      GenerationPlan plan = generator.plan(currentSpec());
      preview.setText(plan.treeView());
      status.setText("Previa atualizada");
    } catch (InvalidProjectSpecException e) {
      preview.setText("Corrija os campos indicados para ver a estrutura.");
      showValidationErrors(e.getErrors());
    } catch (TemplateException e) {
      preview.setText("Falha no modelo: " + e.getMessage());
      status.setText("Erro no modelo");
    }
  }

  private void clearFieldErrors() {
    fields.values().forEach(field -> {
      field.setBorder(fieldBorder);
      field.setToolTipText(null);
    });
    javaVersion.setBorder(UIManager.getBorder("ComboBox.border"));
    license.setBorder(UIManager.getBorder("ComboBox.border"));
  }

  private void showValidationErrors(java.util.List<ValidationError> errors) {
    StringBuilder message = new StringBuilder();
    for (ValidationError error : errors) {
      if (!message.isEmpty()) {
        message.append("; ");
      }
      message.append(error.message());
      JTextField field = fields.get(error.field());
      if (field != null) {
        field.setBorder(BorderFactory.createLineBorder(new java.awt.Color(190, 45, 45), 2));
        field.setToolTipText(error.message());
      }
    }
    status.setText(message.isEmpty() ? "Preencha os campos obrigatorios" : message.toString());
  }

  private void chooseBaseDirectory() {
    JFileChooser chooser = new JFileChooser(baseDirectory.getText());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      baseDirectory.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  private void importSpec() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    try {
      ProjectSpec spec = codec.read(chooser.getSelectedFile().toPath());
      fields.get("projectName").setText(spec.projectName());
      fields.get("artifactId").setText(spec.artifactId());
      fields.get("basePackage").setText(spec.basePackage());
      fields.get("author").setText(spec.author());
      fields.get("description").setText(spec.description());
      fields.get("firstClassName").setText(spec.firstClassName());
      javaVersion.setSelectedItem(spec.javaVersion());
      license.setSelectedItem(spec.license());
      status.setText("Especificacao importada");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "JSON invalido",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void exportSpec() {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new java.io.File("project-spec.json"));
    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    try {
      codec.write(currentSpec(), chooser.getSelectedFile().toPath());
      status.setText("Especificacao exportada");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Falha ao exportar",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void generateProject() {
    ProjectSpec spec = currentSpec();
    Path base;
    try {
      generator.plan(spec);
      base = Path.of(baseDirectory.getText());
      if (!Files.isDirectory(base)) {
        throw new IOException("Selecione uma pasta base existente.");
      }
    } catch (InvalidProjectSpecException e) {
      showValidationErrors(e.getErrors());
      tabs.setSelectedIndex(0);
      return;
    } catch (TemplateException | IOException | java.nio.file.InvalidPathException e) {
      status.setText(e.getMessage());
      return;
    }

    Path target = base.resolve(spec.artifactId());
    boolean overwrite = false;
    if (Files.isDirectory(target)) {
      try (var children = Files.list(target)) {
        if (children.findAny().isPresent()) {
          int choice = JOptionPane.showConfirmDialog(this,
              "A pasta de destino nao esta vazia. Deseja sobrescrever os arquivos?",
              "Confirmar sobrescrita", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (choice != JOptionPane.YES_OPTION) {
            return;
          }
          overwrite = true;
        }
      } catch (IOException e) {
        status.setText("Nao foi possivel ler a pasta de destino");
        return;
      }
    }
    startGeneration(spec, base, overwrite);
  }

  private void startGeneration(ProjectSpec spec, Path base, boolean overwrite) {
    setBusy(true, "Gerando projeto e executando testes Maven...");
    tabs.setSelectedIndex(3);
    new SwingWorker<GenerationOutcome, Void>() {
      @Override
      protected GenerationOutcome doInBackground() throws Exception {
        GenerationManifest manifest = generator.generate(spec, base, overwrite);
        MavenVerificationResult verification = manifest.successful()
            ? verifier.verify(base.resolve(spec.artifactId())) : null;
        return new GenerationOutcome(manifest, verification);
      }

      @Override
      protected void done() {
        try {
          GenerationOutcome outcome = get();
          StringBuilder text = new StringBuilder();
          text.append(outcome.manifest().successful() ? "Geracao concluida\n"
              : "Geracao incompleta\n");
          text.append("Arquivos gravados: ").append(outcome.manifest().files().size())
              .append('\n');
          outcome.manifest().errors().forEach(error -> text.append("Erro: ").append(error)
              .append('\n'));
          if (outcome.verification() != null) {
            text.append("\nMaven: ").append(outcome.verification().successful()
                ? "BUILD SUCCESS" : "BUILD FAILED").append('\n');
            if (!outcome.verification().stdout().isBlank()) {
              text.append(outcome.verification().stdout());
            }
            if (!outcome.verification().stderr().isBlank()) {
              text.append("\n stderr:\n").append(outcome.verification().stderr());
            }
            if (outcome.verification().timedOut()) {
              text.append("\nVerificacao excedeu o tempo limite.");
            }
          }
          result.setText(text.toString());
          status.setText(outcome.verification() == null
              ? "Geracao finalizada" : outcome.verification().successful()
                  ? "Projeto gerado e validado" : "Projeto gerado, mas os testes falharam");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          result.setText("Geracao interrompida.");
          status.setText("Geracao interrompida");
        } catch (ExecutionException e) {
          result.setText("Falha: " + e.getCause().getMessage());
          status.setText("Falha durante a geracao");
        } finally {
          setBusy(false, status.getText());
        }
      }
    }.execute();
  }

  private void setBusy(boolean busy, String message) {
    generateButton.setEnabled(!busy);
    progress.setVisible(busy);
    status.setText(message);
  }

  private record GenerationOutcome(GenerationManifest manifest,
      MavenVerificationResult verification) {
  }
}