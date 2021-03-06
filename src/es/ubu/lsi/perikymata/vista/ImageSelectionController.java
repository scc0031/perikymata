package es.ubu.lsi.perikymata.vista;

/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;

import es.ubu.lsi.perikymata.MainApp;
import ij.io.Opener;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class ImageSelectionController {

	/**
	 * preview of the full tooth Image.
	 */
	@FXML
	private ImageView previewImage;

	/**
	 * List of selected images to stitch.
	 */
	@FXML
	private ListView<String> filesListView;

	/**
	 * Reference to the main application.
	 */
	private MainApp mainApp;

	////////////////////// Status Elements////////////////////

	/**
	 * Current status, tells to the user if a Thread is running.
	 */
	@FXML
	private Label status;

	/**
	 * Loading gif.
	 */
	@FXML
	private ImageView loading;

	/////////////////////////////////////////////////////////////
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		previewImage.fitHeightProperty().bind(((Pane) previewImage.getParent()).heightProperty());
		previewImage.fitWidthProperty().bind(((Pane) previewImage.getParent()).widthProperty());

		// Loads loading gif.
		loading.setImage(new Image(this.getClass().getResource("/rsc/482.gif").toExternalForm()));
		loading.setVisible(false);
	}

	/**
	 * Opens a FileChooser to let the user select Image to load.
	 */
	@FXML
	private void handleOpen() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show open file dialog
		File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

		if (file != null) {

			java.awt.Image full = new Opener().openImage(file.getParent(), file.getName()).getImage();

			try (FileOutputStream fileStream = new FileOutputStream(
					new File(Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png").toString()))) {

				Files.copy(file.toPath(), fileStream);
			} catch (IOException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur opening full image.", e);
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Error opening full image");
				alert.setHeaderText("Can't open or copy full image.\n");
				alert.setContentText(
						"Can't open full image or copy it into the project folder.\n" + "file was: " + file.toString());
				alert.showAndWait();
			}

			mainApp.setFullImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
			previewImage.setImage(mainApp.getFullImage());
			mainApp.setFilteredImage(mainApp.getFullImage());

		}
	}

	/**
	 * Opens a FileChooser to let the user select multiple Images to load.
	 */
	@FXML
	private void handleOpenMultiple() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show open file dialog
		List<File> list = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());
		for (File file : list) {
			if (file != null) {
				mainApp.getFilesList().add(file.getName());

				try (FileOutputStream fileStream = new FileOutputStream(
						new File(Paths.get(mainApp.getProjectPath(), "Fragments").toString(), file.getName()))) {

					Files.copy(file.toPath(), fileStream);
				} catch (IOException e) {
					mainApp.getLogger().log(Level.SEVERE, "Exception occur opening fragment files.", e);
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Error opening or coping fragments.");
					alert.setHeaderText("Can't open or copy fragment file.\n");
					alert.setContentText("Cant open or copy the image to stitch with path:\n" + file.toString());
					alert.showAndWait();
				}

			}
		}
	}

	/**
	 * Handler that rotates image.
	 */
	@FXML
	private void handleRotate() {
		BufferedImage im = SwingFXUtils.fromFXImage(previewImage.getImage(), null);
		AffineTransform a = new AffineTransform();
		a.translate(0.5 * im.getHeight(), 0.5 * im.getWidth());
		a.rotate(Math.PI / 2);
		a.translate(-0.5 * im.getWidth(), -0.5 * im.getHeight());
		AffineTransformOp op = new AffineTransformOp(a, AffineTransformOp.TYPE_BILINEAR);
		Image i = SwingFXUtils.toFXImage(op.filter(im, null), null);
		previewImage.setImage(i);
		mainApp.setFullImage(i);
		mainApp.setFilteredImage(i);

	}

	/**
	 * Handler that removes a image from the list.
	 */
	@FXML
	private void handleRemoveImage() {
		int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			Paths.get(mainApp.getProjectPath(), "Fragments", filesListView.getItems().get(selectedIndex)).toFile()
					.delete();
			filesListView.getItems().remove(selectedIndex);
		} else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("No Image Selected");
			alert.setContentText("Please select a image in the table to delete it");
			alert.showAndWait();
		}
	}

	/**
	 * Handler that changes to the image filter stage when called.
	 */
	@FXML
	private void nextScreen() {
		mainApp.showImageFilters();
	}

	/**
	 * Handler runs the OpenCV code to Stitch images toguether.
	 */
	@FXML
	private void launchStitcher() {

		changeStatus("Stitching, please wait");
		loading.setVisible(true);

		new Thread(() -> {
			try {
				mainApp.getRootLayout().setDisable(true);
				List<String> tempList = new ArrayList<>();
				StringBuilder tempString = new StringBuilder();
				mainApp.getFilesList()
						.forEach(x -> tempList.add(Paths.get(mainApp.getProjectPath(), "Fragments", x).toString()));
				for (String i : tempList)
					tempString.append(" " + i);
				Process stitcher = Runtime.getRuntime().exec("rsc/stitching/bin/Stitching.exe" + " "
						+ Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png") + " " + tempString);
				int ok;
				// OK exit code is 1.
				if ((ok = stitcher.waitFor()) == 1) {

					java.awt.Image full = new Opener()
							.openImage(Paths.get(mainApp.getProjectPath(), "Full_Image", "Full_Image.png").toString())
							.getImage();

					changeStatus("Stitching completed!");
					loading.setVisible(false);
					mainApp.getRootLayout().setDisable(false);
					Platform.runLater(() -> {
						mainApp.setFullImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
						this.previewImage.setImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
						mainApp.setFilteredImage(SwingFXUtils.toFXImage((BufferedImage) full, null));
					});
				} else {
					changeStatus("Stitching failed.");
					loading.setVisible(false);
					mainApp.getLogger().log(Level.WARNING, "Stitching failed, exit with code: " + ok);

				}
			} catch (IOException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur executing stitcher.", e);

				changeStatus("Stitching failed.");
				loading.setVisible(false);
			} catch (InterruptedException e) {
				mainApp.getLogger().log(Level.SEVERE, "Exception occur waiting for stitching.", e);

				changeStatus("Thread interrupted.");
				loading.setVisible(false);
				Thread.currentThread().interrupt();
			} finally {
				loading.setVisible(false);
				mainApp.getRootLayout().setDisable(false);
			}
		}).start();
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * Also, sets the Images. This is done here because when the method
	 * initialize is called, there is no reference to the mainapp.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		if (mainApp.getFullImage() != null) {
			previewImage.setImage(mainApp.getFullImage());
		}

		// Add observable list data to the table
		filesListView.setItems(mainApp.getFilesList());
	}

	/**
	 * Changes the text of the status label from the Platform because label
	 * can't be changed directly from a thread.
	 * 
	 * @param text
	 */
	private synchronized void changeStatus(String text) {
		Platform.runLater(() -> status.setText(text));
	}
}
