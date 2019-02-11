package org.stacktrace.yo.plexbot.bots.ombi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.stacktrace.yo.plexbot.models.ombi.response.OmbiSearchResponse;
import org.stacktrace.yo.plexbot.models.shared.SearchType;
import org.stacktrace.yo.plexbot.service.ombi.OmbiService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public abstract class OmbiCommand extends BotCommand {

    protected final OmbiService myOmbiService;
    protected final ObjectMapper myMapper = new ObjectMapper();

    public OmbiCommand(OmbiService ombiService, String commandIdentifier, String description) {
        super(commandIdentifier, description);
        myOmbiService = ombiService;
    }


    SendPhoto plexAvailable(Long id, OmbiSearchResponse response) {
        return new SendPhoto()
                .setChatId(id)
                .setPhoto(response.photoPath())
                .setCaption(response.getType() == SearchType.TV ? "TV" : "Movie" + " Is Available")
                .setReplyMarkup(availableKeyboard(response.getPlexUrl()));
    }

    SendMessage nonFound(Long id) {
        return new SendMessage()
                .setChatId(id)
                .setText("No Results");
    }

    SendPhoto requestSearch(Long id, OmbiSearchResponse response) throws IOException, URISyntaxException {
        return new SendPhoto()
                .setChatId(id)
                .setPhoto(response.photoPath())
                .setCaption(response.getType() == SearchType.TV ? "TV" : "Movie" + " Unavailable")
                .setReplyMarkup(requestKeyboard(response));
    }

    InlineKeyboardMarkup availableKeyboard(String url) {
        List<InlineKeyboardButton> keyboardButtons = Lists.newArrayList(
                new InlineKeyboardButton()
                        .setText("View In Plex")
                        .setUrl(url)
        );
        List<List<InlineKeyboardButton>> rows = Lists.newArrayList();
        rows.add(keyboardButtons);
        return new InlineKeyboardMarkup()
                .setKeyboard(rows);
    }

    private InlineKeyboardMarkup requestKeyboard(OmbiSearchResponse req) throws JsonProcessingException {
        List<InlineKeyboardButton> keyboardButtons = null;

        keyboardButtons = Lists.newArrayList(
                new InlineKeyboardButton()
                        .setText("Request")
                        .setCallbackData(myMapper.writeValueAsString(
                                new OmbiRequestCallback().setAction("req")
                                        .setSType(req.getType().getReqValue())
                                        .setId(req.reqId())
                        ))
        );
        List<List<InlineKeyboardButton>> rows = Lists.newArrayList();
        rows.add(keyboardButtons);
        return new InlineKeyboardMarkup()
                .setKeyboard(rows);
    }

    @Data
    @Accessors(chain = true)
    public static final class OmbiRequestCallback {
        private String action;
        private String sType;
        private String id;
    }

}