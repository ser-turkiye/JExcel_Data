package com.ser;

import com.ser.blueline.IDocument;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.IControlContainer;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.scripting.record.RecordScripting;
import org.slf4j.Logger;
import utils.GeneralLib;
import utils.Utils;

public class JSONEditor extends RecordScripting {
    private static Logger log;

    public JSONEditor() {
        super();
        this.log=super.log;
    }
    @Override
    public void onInit() throws EvitaWebException {try {
        Utils.session = getSession();
        Utils.server = Utils.session.getDocumentServer();

        IDocument jsonDoc = null;
        IControl _ObjectDocumentReference = this.getDialog().getFieldByName("ObjectDocumentReference");
        if (_ObjectDocumentReference != null && _ObjectDocumentReference instanceof ITextField) {
            String jsonDocId = Utils.getText(_ObjectDocumentReference);
            if (jsonDocId == null) jsonDocId = "";
            if (!jsonDocId.isEmpty()){
                jsonDoc = Utils.server.getDocument4ID(jsonDocId, Utils.session);
            }
            if (jsonDoc == null) {
                jsonDoc = Utils.createDocument(GeneralLib.ClassIDs.JSONDataDocument);
                if (jsonDoc != null) {
                    String path = System.getProperty("java.io.tmpdir") + "/" + java.util.UUID.randomUUID() + ".json";
                    Utils.saveToFile(path, "");
                    Utils.uploadDefaultRepr(jsonDoc, path);
                    jsonDoc.commit();

                    Utils.setTextfieldValue(_ObjectDocumentReference, jsonDoc.getID());
                }
            }
        }
        IControl editor = this.getDialog().getFieldByName("JSONEditor");
        if (editor instanceof IControlContainer) {
            String strURL = this.getDoxisServer().getRequestURL()
                    + "/xtra/controls/jsoneditor/main.jsp?"
                    + "&new=" + isNew()
                    + "&editable=" + isEditable()
                    + "&docId=" + (jsonDoc == null ? "" : jsonDoc.getID())
                    + "&width=" + editor.getWidth()
                    + "&height=" + editor.getHeight() + "&";
            ((IControlContainer) editor).setURL(strURL, false);
        }
    }catch (Exception ex){
        throw new EvitaWebException(ex.getMessage());
    }}
    public boolean isEditable(){

        if(isNew()){return true;}
        if(getFolder() == null){return false;}
        if(getFolder().findLockInfo() == null){return false;}
        if(getFolder().findLockInfo().getUser() != getSession().getUser().getLogin()){
            return false;
        }
        return true;
    }
}
