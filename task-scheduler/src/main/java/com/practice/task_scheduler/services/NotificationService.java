package com.practice.task_scheduler.services;

public interface NotificationService {
    public boolean saveNotification();
    public boolean sendNotification();
    public boolean sendNotificationViaEmail();
    public boolean deleteNotification(long userId, long notificationId);
    public boolean changeStatus(long userId, long notificationId);
}
