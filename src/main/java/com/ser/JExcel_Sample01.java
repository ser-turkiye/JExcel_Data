package com.ser;

import com.ser.blueline.IDatabase;
import com.ser.blueline.IDocument;
import com.ser.blueline.IInformationObject;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.IControlContainer;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.scripting.record.RecordScripting;
import org.slf4j.Logger;
import utils.GeneralLib;
import utils.ProcessHelper;
import utils.Utils;

import java.nio.file.Files;

public class JExcel_Sample01 extends RecordScripting {
    private static Logger log;

    public JExcel_Sample01() {
        super();
        this.log=super.log;
    }
    @Override
    public void onInit() throws EvitaWebException {try {
        Utils.session = getSession();
        Utils.server = Utils.session.getDocumentServer();
        ProcessHelper helper = new ProcessHelper(Utils.session);

        IDocument jsonDoc = null;
        IInformationObject jsonCfg = getJSONData(GeneralLib.ClassIDs.JExcel_Config,"JExcel_Sample01", helper);
        log.warn(" ************** jsonCfg :::: " + (jsonCfg == null ? "" : jsonCfg.getID()));
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

        IControl editor = this.getDialog().getFieldByName("JExcelEditor");
        if (editor instanceof IControlContainer) {
            String strURL = this.getDoxisServer().getRequestURL()
                    + "/xtra/controls/jspreadsheet-ce/main.jsp?"
                    + "&new=" + isNew()
                    + "&editable=" + isEditable()
                    + "&docId=" + (jsonDoc == null ? "" : jsonDoc.getID())
                    + "&cfgId=" + (jsonCfg == null ? "" : jsonCfg.getDescriptorValue("ObjectDocumentReference", String.class))
                    + "&width=" + editor.getWidth()
                    + "&height=" + editor.getHeight() + "&";
            ((IControlContainer) editor).setURL(strURL, false);
        }
    }catch (Exception ex){
        throw new EvitaWebException(ex.getMessage());
    }}

    static IInformationObject getJSONData(String dbnm, String rfnm, ProcessHelper helper) {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(GeneralLib.ClassIDs.JExcel_Config).append("'")
                .append(" AND ")
                .append(GeneralLib.DescriptorLiterals.ObjectName).append(" = '").append(rfnm).append("'");
        String whereClause = builder.toString();
        log.warn("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = helper.createQuery(new String[]{"D_JEXCEL"}, whereClause, "", 1, false);
        if(informationObjects.length < 1) {return null;}
        return informationObjects[0];
    }
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
