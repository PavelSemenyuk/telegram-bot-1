package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.telegrambot.entity.NotificationTask;
import repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    NotificationTask notificationTask;


    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            if (update.message().text().equals("/start")) {
                String messageText = "Привет " + update.message().chat().username() + ", я БОТ-НАПОМИНАЛКА!" +"\n"
                        + "Напиши заметку в формате <01.01.2022 20:00 Выпить вина!)>, и в нужный день и время, я напомню тебе!"
                        ;
                long chatId = update.message().chat().id();
                SendMessage message = new SendMessage(chatId, messageText);

                telegramBot.execute(message);
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    public void parseMessages(Update update) {
        // проверяем является ли сообщение текстом
        if (update.message().text()!= null ) {
            String messageText = update.message().text();
            // создаем строку образец которая содержит буквы и цифры
            Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})\\s(.+)");
            // выполняем поиск по строке и проверяем удовлитворяет наш текст строку-образец
            Matcher matcher = pattern.matcher(messageText);

            // вытаскиваем отдельно дату и текст проверяем удовлитворяет она нашим пораметрам  и сохраняем в базе
            if (matcher.matches()) {
                String date = matcher.group(1);
                String reminderText = matcher.group(2);
                LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                NotificationTask notificationTask = new NotificationTask(update.message().chat().id(), reminderText, dateTime);
                notificationTaskRepository.save(notificationTask);

                // если все ок то отправляем даноое сообщение
                String messageText2 = update.message().chat().username() + ", Ваше напоминание сохранено!";
                long chatId = update.message().chat().id();
                SendMessage replyMessage = new SendMessage(chatId, messageText2);

                telegramBot.execute(replyMessage);


                // если не ок отправляем это сообщение
            } else {
                String messageText3 = "Неверный формат напоминания. "+ update.message().chat().username() + ", пожалуйста, введите напоминание в формате дд.мм.гггг чч:мм и текст напоминание.";
                long chatId = update.message().chat().id();
                SendMessage replyMessage = new SendMessage(chatId, messageText3);
                telegramBot.execute(replyMessage);

            }
        }
    }


    // метод каждую минуту сравнивает текушее время и время из базы
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationTasks() {
        // Получаем текущее время
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        // Ищем записи в базе данных, у которых время отправки совпадает с текущим
        List<NotificationTask> notificationTasks = notificationTaskRepository.findByNotificationTime(currentTime);
        for (NotificationTask task : notificationTasks) {
            sendNotification(task);
        }
    }

    // метод для отправки сообшения нужному пользователю и удалению этого сообщения из базы
    private void sendNotification(NotificationTask task) {
        Long chatId = task.getChatId();
        String notificationText = task.getNotificationText();

        SendMessage message = new SendMessage(chatId, notificationText);
        telegramBot.execute(message);
        notificationTaskRepository.delete(task);

    }


}
