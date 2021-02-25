package bot.sampo.service;

import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminRequestService {

    private final DancerRepository dancerRepository;
    private final ConfigRepository configRepository;

    public boolean isAdmin(Long userId) {
        if (userId == null)
            return false;

        var dancer = dancerRepository.getDancerById(userId);

        if (dancer == null)
            return false;

        return dancer.isAdmin();
    }

    public boolean isAdminCommand(Update request) {

        if (request.getMessage() == null || request.getMessage().getText() == null)
            return false;

        var text = request.getMessage().getText();
        return text.startsWith("#");
    }

    public List<SendMessage> handle(Update request) {

        var userId = request.getMessage().getChatId();

        var text = request.getMessage().getText();

        if (text.startsWith("#sc")) {
            var date = LocalDate.ofEpochDay(Long.parseLong(text.replace("#sc", "")));
            configRepository.setCurrentSampoDate(date);
            return List.of(replyToTelegramMsg(userId, "set current sampo date successful to " + date));
        }
        if (text.startsWith("#help")) {
            return List.of(replyToTelegramMsg(userId, """
                                        
                    Command help:
                                        
                    - change current sampo date
                        #sc<Days Since 1970-01-01> for example 'sc42790' that will set date to February 25, 2021. 
                        Use https://www.epochconverter.com/seconds-days-since-y0
                                        
                    """));
        }

        return List.of(replyToTelegramMsg(userId, "admin command unrecognized"));
    }

    private SendMessage replyToTelegramMsg(long chatID, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.setText(text);
        return message;
    }
}
