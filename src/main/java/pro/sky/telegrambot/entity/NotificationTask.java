package pro.sky.telegrambot.entity;



import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Id;

@Entity
@Table(name = "notification_task")
public class NotificationTask {

    @Id


    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "notification_text")
    private String notificationText;

    @Column(name = "notification_date")
    private LocalDateTime notificationDate;

    public NotificationTask() {
    }

    public NotificationTask( Long chatId, String notificationText, LocalDateTime notificationDate) {
        this.chatId = chatId;
        this.notificationText = notificationText;
        this.notificationDate = notificationDate;
    }



    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return  Objects.equals(chatId, that.chatId) && Objects.equals(notificationText, that.notificationText) && Objects.equals(notificationDate, that.notificationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash( chatId, notificationText, notificationDate);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "chatId=" + chatId +
                ", notificationText='" + notificationText + '\'' +
                ", notificationDate=" + notificationDate +
                '}';
    }
}
