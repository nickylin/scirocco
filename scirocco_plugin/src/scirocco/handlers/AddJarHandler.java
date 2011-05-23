package scirocco.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.android.ide.eclipse.scirocco.AdtPlugin;

public class AddJarHandler extends AbstractHandler {
	private IProject project;
	private IJavaProject javaProject;
	private IPackageFragmentRoot sourceFolder;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		project = getProjectFromSelection(selection);
		javaProject = JavaCore.create(project);
		IFolder libFolder = createLibFolder();

		MessageDialog.openInformation(window.getShell(), "CommandSample ƒvƒ‰ƒOƒCƒ“",
				"Hello, Eclipse world");
		// TODO Auto-generated method stub
		return null;
	}

	static String LIB_DIRECTORY = "lib";
	static String DISTRIBUTION_LIBS_DIRECTORY = "libs/";
	static String JAR_ROBOTIUM= "robotium.jar";
	static String JAR_SCIROCCO= "scirocco.jar";
	
	private IFolder createLibFolder() {
		try {
			IFolder libFolder = project.getFolder(LIB_DIRECTORY);
			if (!libFolder.exists()) {
				libFolder.create(false, true, null);
			}
			IFile file;
			file = libFolder.getFile(JAR_ROBOTIUM);
			if (!file.exists()) {
				addFile(file, AdtPlugin.readEmbeddedFile(DISTRIBUTION_LIBS_DIRECTORY + JAR_ROBOTIUM));
			}
			file = libFolder.getFile(JAR_SCIROCCO);
			if (!file.exists()) {
				addFile(file, AdtPlugin.readEmbeddedFile(DISTRIBUTION_LIBS_DIRECTORY + JAR_SCIROCCO));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Creates a file from a data source.
     * @param dest the file to write
     * @param source the content of the file.
     * @param monitor the progress monitor
     * @throws CoreException
     */
    private void addFile(IFile dest, byte[] source) throws CoreException {
        if (source != null) {
            // Save in the project
            InputStream stream = new ByteArrayInputStream(source);
            dest.create(stream, false /* force */,null);
        }
    }
    
	private IProject getProjectFromSelection(ISelection selection) {
		IStructuredSelection iss = (IStructuredSelection) selection;
		Object obj = iss.getFirstElement();
		IProject prj = null;
		if (obj instanceof IFile) {
			// IFile ‚©‚ç IProject‚ðŽæ“¾‚·‚é
			IFile f = (IFile) obj;
			prj = f.getProject();

		} else if (obj instanceof IFolder) {
			// IProject ‚©‚ç IProject‚ðŽæ“¾‚·‚é
			IFolder f = (IFolder) obj;
			IWorkspace iw = f.getWorkspace();
			IWorkspaceRoot iwr = iw.getRoot();
			prj = iwr.getProject();

		} else if (obj instanceof IResource) {
			// IResource ‚©‚ç IProject‚ðŽæ“¾‚·‚é
			IResource res = (IResource) obj;
			prj = res.getProject();

		} else if (obj instanceof IJavaElement) {
			// IJavaElement ‚©‚ç IProject‚ðŽæ“¾‚·‚é
			IJavaElement ij = (IJavaElement) obj;
			IJavaProject jprj = ij.getJavaProject();
			prj = jprj.getProject();
		}
		return prj;

	}

}
