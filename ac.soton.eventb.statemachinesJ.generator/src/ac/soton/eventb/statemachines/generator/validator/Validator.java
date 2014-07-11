package ac.soton.eventb.statemachines.generator.validator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;

import ac.soton.eventb.statemachines.diagram.part.ValidateAction;
import ac.soton.eventb.statemachines.diagram.providers.StatemachinesMarkerNavigationProvider;
import ac.soton.eventb.emf.diagrams.generator.IValidator;

public class Validator implements IValidator {

	@Override
	public boolean validate(DiagramDocumentEditor diagramDocumentEditor) throws Exception {
		// run validation
		ValidateAction action = new ValidateAction(diagramDocumentEditor.getSite().getPage());
		action.run();
		return hasMarkers(diagramDocumentEditor)==false;
	}
		
	/**
	 * Returns a string of errors from validation markers for file.
	 * 
	 * @param diagramDocumentEditor
	 * @return string of errors
	 * 
	 */
	@Override
	public String getValidationErrors(DiagramDocumentEditor diagramDocumentEditor) {
		IMarker[] markers;
		try {
			markers = getFile(diagramDocumentEditor).findMarkers(
					getMarkerType(),
					true,
					IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			return "ERRORS UNKNOWN: COULD NOT RETRIEVE VALIDATION ERROR MARKERS FROM FILE";
		}
		StringBuilder errors = new StringBuilder();
		for (IMarker marker : markers) {
			//int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			if (true) //severity == IMarker.SEVERITY_ERROR)
				errors.append("\n"
						+ marker.getAttribute(IMarker.MESSAGE, "unknown error"));
		}
		return errors.toString();
	}
	
	
	/////////// not in interface (currently) /////////////

	public String getMarkerType() {
		return StatemachinesMarkerNavigationProvider.MARKER_TYPE;
	}

	public boolean hasMarkers(DiagramDocumentEditor diagramDocumentEditor) throws CoreException{
		return getFile(diagramDocumentEditor).findMarkers(getMarkerType(),true,IResource.DEPTH_ZERO).length>0;
	}
	
	public IFile getFile(DiagramDocumentEditor diagramDocumentEditor){
		return WorkspaceSynchronizer.getFile(diagramDocumentEditor.getDiagram().eResource());
	}
}
