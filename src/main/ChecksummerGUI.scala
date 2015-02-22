/**
 * File: ChecksummerGUI.scala
 * Package: main
 * Autore: Francesco Burato
 * Creazione: 04/lug/2013
 */
package main

import checksum.{ FileCheckerFactory, FileChecksummer }
import checkmanager.{ CheckManager, ThreadBasedManager, ActorBasedManager, ODActorManager , Configurator }
import workers.{ ProcessFiles, Completion, Terminate }
import scala.actors.Actor.{ actor, receive }
import swing._
import swing.event._
import GridBagPanel._
import java.io.{ File, PrintWriter }
import javax.swing.{ JSpinner, SpinnerNumberModel, JProgressBar, JTextArea}
import javax.swing.text.DefaultCaret

object ChecksummerGUI extends SimpleSwingApplication {
  val executors = "thread" :: "actor" :: "odactor" :: Nil
  val checksums = "update" :: "md5" :: Nil
  // oggetti grafici importanti
  val startButton = new Button("Start")
  val resetButton = new Button("Reset")
  val clearlogButton = new Button("Clear Log")
  val execCombo = new ComboBox(executors)
  val checkCombo = new ComboBox(checksums)
  val defaultWorkers = Configurator.workerPoolSize
  val defaultBlock = Configurator.bufferSize
  val workersSpinner = new JSpinner(new SpinnerNumberModel(defaultWorkers, 1, 100, 1))
  val blockSpinner = new JSpinner(new SpinnerNumberModel(defaultBlock, 1, 16384, 512))
  val resultArea = new JTextArea("")
  val choosedDirectory = new TextField("")
  val choosedOutputFile = new TextField("")
  val openButton = new Button("Open")
  val saveButton = new Button("Save as")
  val outputfilechooser = new FileChooser()
  val inputdirectorychooser = new FileChooser()
  val progressBar = new JProgressBar()
  var outputfile: Option[File] = None
  var directory: Option[File] = None
  // attore di aggiornamento della progressBar
  val progActor = actor {
    var cont = true
    while (cont) {
      receive {
        case ProcessFiles(tot) =>
          progressBar setMaximum tot
          progressBar setValue 0
          progressBar setVisible true
          var completed = 0
          while (completed < tot) {
            receive {
              case Completion(i) =>
                completed += 1
                progressBar.setValue(completed)
            }
          }
        case Terminate() => cont = false
      }
    }
  }
  // richiamo l'inizializzatore degli oggetti grafici
  init()
  // pannello principale 
  lazy val ui = new GridBagPanel {
    val c = new Constraints
    c.fill = Fill.Both
    c.weightx = 0.2
    c.weighty = 1
    c.gridx = 0
    c.gridy = 0
    layout(createParameterPanel) = c

    c.fill = Fill.Both
    c.weightx = 1
    c.weighty = 1
    c.gridx = 1
    c.gridy = 0
    layout(createResultPanel) = c

    c.fill = Fill.Horizontal
    c.weightx = 1
    c.weighty = 0
    c.gridx = 0
    c.gridy = 1
    c.gridwidth = 2
    layout(createProgressPanel) = c

    c.gridy = 2
    layout(createOutputFilePanel) = c
  }

  override def quit() {
    progActor ! Terminate()
    super.quit
  }

  def top = new MainFrame {
    self.setMinimumSize(new java.awt.Dimension(800,600))
    title = "File Checksummer"
    contents = ui
    listenTo(saveButton)
    listenTo(startButton)
    listenTo(openButton)
    listenTo(resetButton)
    listenTo(clearlogButton)
    reactions += {
      case ButtonClicked(b) =>
        if (b == saveButton) {
          val result = outputfilechooser.showSaveDialog(null)
          if (result == FileChooser.Result.Approve) {
            val f = outputfilechooser.selectedFile
            choosedOutputFile.text = f.getAbsolutePath()
            outputfile = Some(f)
          }
        } else if (b == openButton) {
          val result = inputdirectorychooser.showOpenDialog(null)
          if (result == FileChooser.Result.Approve) {
            val f = inputdirectorychooser.selectedFile
            choosedDirectory.text = f.getAbsolutePath()
            directory = Some(f)
          }
        } else if (b == startButton) {
          setCommandsEnabled(false)
          actor {
            val output = outputfile match {
              case None =>
                resultArea append "*** ERROR: Output file not selected ***\n"; ""
              case Some(x) => resultArea append "Output file selected:" + x.getAbsolutePath() + "\n"; x.getAbsolutePath()
            }
            val input = directory match {
              case None =>
                resultArea.append("*** ERROR: Input path not provided ***\n"); ""
              case Some(x) => resultArea append "Directory selected:" + x.getAbsolutePath() + "\n"; x.getAbsolutePath()
            }
            if (input != "" && output != "")
              execute(input, outputfile.get)
            else
              resultArea append "*** EXECUTION ABORTED ***\n\n"
            setCommandsEnabled(true)
          }
        } else if(b == resetButton) {
          // reset di tutti i campi
          workersSpinner.setValue(defaultWorkers)
          blockSpinner.setValue(defaultBlock)
          resultArea.setText("")
          outputfile = None
          directory = None
          choosedDirectory.text = ""
          choosedOutputFile.text = ""
          execCombo.selection.index = 0
          checkCombo.selection.index = 0
          progressBar setVisible false
        } else if(b == clearlogButton) {
          resultArea setText ""
        }
    }
  }

  private def execute(in: String, out: File) {
    // controllo di esistenza e dei permessi del file
    if (!out.exists()) {
      if (!out.createNewFile())
        resultArea append "*** ERROR: " + out.getAbsolutePath() + " is not creatable ***\n"
    } else if (!out.canWrite()) {
      resultArea append "*** ERROR: " + out.getAbsolutePath() + " is not creatable ***\n"
    }
    // controllo dei valori degli spinner
    val workersOpt = parseInt(workersSpinner.getValue().toString)
    val blockSizeOpt = parseInt(blockSpinner.getValue().toString)
    val workers = workersOpt match {
      case None => {
        resultArea append "*** ERROR: Number of workers must be positive ***\n"
        -1
      }
      case Some(x) => if (x > 0) x else {
        resultArea append "*** ERROR: Number of workers must be positive ***\n"
        -1
      }
    }
    val blockSize = blockSizeOpt match {
      case None => {
        resultArea append "*** ERROR: Block size must be positive ***\n"
        -1
      }
      case Some(x) => if (x > 0) x else {
        resultArea append "*** ERROR: Block size must be positive ***\n"
        -1
      }
    }
    if (out.exists() && out.canWrite() && workers != -1 && blockSize != -1) {
      // imposto i parametri di esecuzione
      Configurator.bufferSize = blockSize
      Configurator.workerPoolSize = workers
      val mode = execCombo.item
      val checker = checkCombo.item
      // Notifico l'inizio dell'elaborazione
      resultArea append "--- NEW EXCECUTION STARTED ---\n" +
        "- Worker pool size: " + Configurator.workerPoolSize + "\n" +
        "- Block size: " + Configurator.bufferSize + "\n" +
        "- Execution model: " + mode + "\n" +
        "- Check algorithm: " + checker + "\n\n"
      // istanzio gli oggetti per l'elaborazione
      val f = FileCheckerFactory.getInstance(checker) match {
        case None => null
        case Some(c) => c
      }
      val manager: CheckManager = mode match {
        case "actor" => new ActorBasedManager(f, progActor)
        case "thread" => new ThreadBasedManager(f, progActor)
        case "odactor" => new ODActorManager(f,progActor)
      }

      // eseguo l'elaborazione
      resultArea append "* Start time: " + new java.util.Date + "\n"
      val start = System.currentTimeMillis()
      val list = manager.execute(in)
      val stop = System.currentTimeMillis()
      resultArea append "* Stop time: " + new java.util.Date + "\n\n" +
        "--- Processed "+ list.length +" files  in " + (stop - start) + " ms ---\n\n" +
        "- Writing output file -\n"
      if (list.length > 0) {
        val printer = new PrintWriter(out)
        for ((file, res) <- list) {
          printer.write(res + " <-- " + file + "\n")
        }
        printer.close()
      }
      resultArea append "--- EXECUTION COMPLETED ---\n\n"
    } else
      resultArea append "*** EXECUTION ABORTED ***\n\n"
  }

  private def parseInt(s: String): Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _: Throwable => None
  }

  private def setCommandsEnabled(b: Boolean) {
    startButton.enabled = b
    resetButton.enabled = b
    execCombo.enabled = b
    checkCombo.enabled = b
    workersSpinner setEnabled b
    blockSpinner setEnabled b
    openButton.enabled = b
    saveButton.enabled = b
    clearlogButton.enabled = b
  }
  private def init() {
    workersSpinner.setEditor(new JSpinner.NumberEditor(workersSpinner, "##"));
    blockSpinner.setEditor(new JSpinner.NumberEditor(blockSpinner, "##"));
    inputdirectorychooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    inputdirectorychooser.multiSelectionEnabled = false
    outputfilechooser.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    outputfilechooser.multiSelectionEnabled = false
    choosedDirectory.editable = false
    resultArea setEditable false
    choosedOutputFile.editable = false
    progressBar setVisible false
  }

  private def createParameterPanel = {
    val panel = new FlowPanel(FlowPanel.Alignment.Center)(

      new GridPanel(7, 1) {
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += new Label("Execution:")
          contents += execCombo
        }
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += new Label("Algorithm:")
          contents += checkCombo
        }
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += new Label("Pool:")
          contents += Component.wrap(workersSpinner)
        }
        contents += new BoxPanel(Orientation.Horizontal) {
          contents += new Label("Block Size:")
          contents += Component.wrap(blockSpinner)
        }
        contents += startButton
        contents += resetButton
        contents += clearlogButton
      })
    panel.border = Swing.TitledBorder(Swing.EtchedBorder(Swing.Raised), "Parameters")
    panel
  }
  private def createResultPanel =
    new BoxPanel(Orientation.Vertical) {
      contents += {
        val p = new ScrollPane(Component.wrap(resultArea))
        val caret = resultArea.getCaret().asInstanceOf[DefaultCaret]
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
        p
      }
      border = Swing.TitledBorder(Swing.EtchedBorder(Swing.Raised), "Log")
    }
  private def createOutputFilePanel = new GridPanel(2, 1) {
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Directory:")
      contents += choosedDirectory
      contents += openButton
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Output file:")
      contents += choosedOutputFile
      contents += saveButton
    }
    border = Swing.TitledBorder(Swing.EtchedBorder(Swing.Raised), "Files selector")
  }
  private def createProgressPanel =
    new BoxPanel(Orientation.NoOrientation) {
      contents += Component wrap progressBar
      border = Swing.EmptyBorder(5)
    }
}