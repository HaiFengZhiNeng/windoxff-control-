package com.ocean.mvp.library.net;

import com.ocean.mvp.library.utils.JsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过网络发送的文件消息
 *  eg： FileMessage message = new FileMessage(getSelectRobot(), text)  (ps getSelectRobot() 在BasePresenter, text为想要发送的内容)
 *      message.addFile（filename）   添加需要发送的文件列表
 *
 * 如果想通过互联网发送此TextMessage，就可以message.setSendMethod(FileMessage.SEND_METHOD_INTERNET);
 *
 * 如果想通过局域网发送此TextMessage，就可以message.setSendMethod(FileMessage.SEND_METHOD_LAN);
 *
 *      局域网下只能通过TCP发送文件  就可以message.setTransportType(NetMessage.TRANSPORT_TYPE_TCP);
 *
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class FileMessage extends TextMessage {


    public FileMessage() {
        super();
    }

    public FileMessage(String url) {
        super(url);
    }

    public FileMessage(String hostIp, int udpPort) {
        super(hostIp, udpPort);
    }

    public FileMessage(String hostIp, int udpPort, String content) {
        super(hostIp, udpPort, content);
    }

    public FileMessage(long moduleId, String moduleName, String actionName, String extraName) {
        super(moduleId, moduleName, actionName, extraName);
    }


    /**
     * 分发消息消息需要
     * @param moduleId  接受模块的id
     * @param moduleName 接受模块的名称
     * @param actionName    接受广播的action
     * @param dataExtraName 广播中数据的Key
     * @param fileExtraName 广播中文件下载路径的Key
     * @throws JSONException
     */
    public void setRequestAction(long moduleId, String moduleName, String actionName, String dataExtraName, String fileExtraName){
        checkModuleId(moduleId);
        checkString(moduleName);
        checkString(actionName);
        checkString(dataExtraName);
        checkString(fileExtraName);
        if(dataExtraName.equals(fileExtraName)) throw new RuntimeException("广播的数据key和路径key不能相同");
        try {
            jsonBox = new JSONObject();
            jsonBox.put("module_id",moduleId);
            jsonBox.put("module_name",moduleName);
            jsonBox.put("action_name",actionName);
            jsonBox.put("data_extra_name",dataExtraName);
            jsonBox.put("file_extra_name",fileExtraName);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<File> filePaths = new ArrayList<File>();


    public void addFile(String filePath) {
        initFilesJson(filePath);
    }

    public void addFiles(List<String> fileList){
        initFilesJson(fileList);
    }


    public List<File> getFilePaths() {
        return filePaths;
    }

    private void initFilesJson (String filePath) {
        if(jsonBox == null) jsonBox = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            long totalLength = 0;
            File file = new File(filePath);
            long length = file.length();
            totalLength += length;
            JsonBuilder builder = new JsonBuilder().append("file_length", length);
            int index = filePath.lastIndexOf("/");
            String fileName = filePath.substring(index + 1, filePath.length());
            jsonArray.put(builder.append("file_name", fileName).builder());
            filePaths.add(file);
            jsonBox.put("total_length", totalLength);
            jsonBox.put("files", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void initFilesJson (List<String> fileList) {
        if(jsonBox == null) jsonBox = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();
            long totalLength = 0;
            for(String filePath :fileList)  {
                File file = new File(filePath);
                long length = file.length();
                totalLength += length;
                JsonBuilder builder = new JsonBuilder().append("file_length", length);
                int index = filePath.lastIndexOf("/");
                String fileName = filePath.substring(index + 1, filePath.length());
                jsonArray.put(builder.append("file_name", fileName).builder());
                filePaths.add(file);
            }
            jsonBox.put("total_length", totalLength);
            jsonBox.put("files", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

