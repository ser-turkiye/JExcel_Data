package utils;

import com.ser.blueline.*;
import com.ser.blueline.metaDataComponents.IArchiveClass;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.context.IFolderContext;
import com.ser.evITAWeb.api.context.IScriptingContext;
import com.ser.evITAWeb.api.context.ISourceContext;
import com.ser.evITAWeb.api.context.ITaskContext;
import com.ser.evITAWeb.api.controls.*;
import com.ser.foldermanager.IFolder;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    public static ISession session = null;
    public static IDocumentServer server = null;

    public static IDocument createDocument(String docClassId)  {
        IArchiveClass ac = server.getArchiveClass(docClassId, session);
        IDatabase db = session.getDatabase(ac.getDefaultDatabaseID());
        IDocument rtrn = server.getClassFactory().getDocumentInstance(db.getDatabaseName(), ac.getID(), "0000" , session);
        return rtrn;
    }
    public static String getBase64Encode(String text){
        return new String(Base64.getEncoder().encode(text.getBytes()));
    }
    public static void uploadDefaultRepr(IDocument tdoc, String path) throws Exception {
        if(path.isEmpty()) {return;}
        IRepresentation irep = (tdoc.getDefaultRepresentation() >= 0 ? tdoc.getRepresentation(tdoc.getDefaultRepresentation()) : null);
        if(irep == null) {
            irep = tdoc.addRepresentation("." + FilenameUtils.getExtension(path), FilenameUtils.getBaseName(path));
        }
        irep.removeAllPartDocuments();
        irep.addPartDocument(path);
        tdoc.setDefaultRepresentation(tdoc.getRepresentationList().length - 1);
    }
    public static void saveToFile(String path, String content) throws Exception {
        FileWriter save = new FileWriter(path);
        save.write(content);
        save.close();
    }
    public static IFolder getSourceFolder(ISourceContext srcContext){
        IScriptingContext source = srcContext.getSourceContext();
        if(source == null) return null;
        if(!(source instanceof IFolderContext)) return null;
        IFolderContext folderContext = (IFolderContext) source;
        return folderContext.getFolder();
    }
    public static IInformationObject getSourceInformationObject(ISourceContext srcContext){
        IScriptingContext source = srcContext.getSourceContext();
        if(source == null) return null;
        if((source instanceof ITaskContext)) {
            ITaskContext context = (ITaskContext) source;
            return context.getTask();
        }else if(source instanceof IFolderContext){
            IFolderContext folderContext = (IFolderContext) source;
            return folderContext.getFolder();
        }
        return null;
    }
    public static IControl getControl(IDialog dialog, String controlName) {
        if (dialog == null) {
            throw new IllegalArgumentException("missing parameter 'dialog'");
        } else if (controlName == null) {
            log.warn("missing control name");
            return null;
        } else {
            IControl c = dialog.getFieldByName(controlName);
            if (c == null) {
                log.warn("missing control <{}>", controlName);
            } else {
                log.debug("found control <{}>", controlName);
            }

            return c;
        }
    }

    public static void setSelectionBoxValue(IControl iControl, String value) {
        if (iControl == null) {
            log.warn("missing parameter iControl");
        } else if (iControl instanceof ISelectionBox) {
            ISelectionBox sb = (ISelectionBox)iControl;
            sb.setSelectedItemValue(value);
        } else {
            log.warn("setSelectionBoxValue not supported for control <{}>", iControl.getName());
        }

    }
    public static void setSelectionBoxValue(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of selection box", controlName);
        } else {
            setSelectionBoxValue(c, value);
        }

    }
    public static void setCheckBox(IDialog dlg, String controlName , String value){
        IControl c = getControl(dlg, controlName);
        ICheckbox checkBoxControl = (ICheckbox) c;
        if(value.equals("true")){
            checkBoxControl.setChecked(true);
        }
    }
    public static String getSelectionBoxValue(IControl iControl) {
        if (iControl == null) {
            log.warn("missing parameter iControl");
            return "";
        }
        if (iControl instanceof ISelectionBox) {
            ISelectionBox sb = (ISelectionBox)iControl;
            return String.join(" ", sb.getValues());
        }
        return "";
    }
    public static String getText(IControl iControl) {
        if (iControl == null) {
            log.warn("missing parameter iControl");
            return "";
        }
        if (iControl instanceof ITextField) {
            ITextField tf = (ITextField)iControl;
            return tf.getText();
        }
        return "";
    }

    public static void setText(IDialog dlg, String controlName, String value) {
        log.debug("set text for control <{}>", controlName);
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set text value", controlName);
        } else {
            if (c instanceof ITextField) {
                setTextfieldValue(c, value);
            } else if (c instanceof IDate) {
                setDateValue(dlg, controlName, value);
            } else if (c instanceof ISelectionBox) {
                setSelectionBoxValue(c, value);
            } else if (c instanceof ILabel) {
                setLabelValue(dlg, controlName, value);
            } else if (c instanceof IMultiLineEdit) {
                setMultiLineValue(dlg, controlName, value);
            }
            else if(c instanceof IMultiValueSelectionBox){
                setMultiValueSelectionBox(dlg,controlName,value);
            }else if(c instanceof ICheckbox){
                setCheckBox(dlg,controlName,value);
            }
            else {
                log.warn("setText not supported for control <{}>", controlName);
            }

        }
    }

    public static void setLabelValue(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of label", controlName);
        } else if (c instanceof ILabel) {
            ILabel tf = (ILabel)c;
            tf.setText(value);
        } else {
            log.warn("setLabelValue not supported for control <{}>", controlName);
        }

    }

    public static void setMultiLineValue(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of multi line edit", controlName);
        } else if (c instanceof IMultiLineEdit) {
            IMultiLineEdit tf = (IMultiLineEdit)c;
            tf.setText(value);
        } else {
            log.warn("setMultiLineValue not supported for control <{}>", controlName);
        }

    }

    public static void setMultiValueSelectionBox(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of multi line edit", controlName);
        } else if (c instanceof IMultiValueSelectionBox) {

            value = value.replace("[","").replace("]","");

            IMultiValueSelectionBox tf = (IMultiValueSelectionBox)c;
            tf.setSelectedItems( Arrays.asList(value.split(",")));

            //tf.setSelectedItems(Collections.singletonList(value));
        } else {
            log.warn("setMultiLineValue not supported for control <{}>", controlName);
        }

    }



    public static String formatDate(String val){
        final String DOXIS_DATE_FORMAT = "yyyyMMdd";
        List<SimpleDateFormat> knownPatterns = new ArrayList<>();
        knownPatterns.add(new SimpleDateFormat(DOXIS_DATE_FORMAT));
        for (SimpleDateFormat pattern : knownPatterns) {
            try {
                // Take a try
                pattern.setLenient(false);
                Date d = pattern.parse(val);
                return new SimpleDateFormat("dd.MM.YYYY").format(d);
            } catch (java.text.ParseException pe) {
                // Loop on
            }
        }
        return val;

    }
    public static void setDateValue(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        value = formatDate(value);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of date", controlName);
        } else if (c instanceof IDate) {
            IDate iDate = (IDate)c;
            iDate.setText(value);
        } else {
            log.warn("setDateValue not supported for control <{}>", controlName);
        }

    }
    public static void setTextfieldValue(IDialog dlg, String controlName, String value) {
        IControl c = getControl(dlg, controlName);
        if (c == null) {
            log.warn("control <{}> not found, failed to set value of text field", controlName);
        } else {
            setTextfieldValue(c, value);
        }

    }

    public static void setTextfieldValue(IControl iControl, String value) {
        if (iControl == null) {
            log.warn("missing parameter iControl");
        } else if (iControl instanceof ITextField) {
            ITextField tf = (ITextField)iControl;
            tf.setText(value);
        } else {
            log.warn("setTextfieldValue not supported for control <{}>", iControl.getName());
        }

    }
}
