package com.xxf.i18n.plugin.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * 支持ios的.m文件自动抽取字符串
 * Created by xyw on 2023/5/23.
 */
public class MessageUtils {
    public static void showAlert(AnActionEvent e,String message){
        Project currentProject = e.getProject();
        Messages.showMessageDialog(currentProject,
                message,
                "提示",
                Messages.getInformationIcon());
    }
    public static void showNotify(String msg) {
        Notifications.Bus.notify(new Notification("XXFi18nString", "提示", msg, NotificationType.INFORMATION));
    }
}
