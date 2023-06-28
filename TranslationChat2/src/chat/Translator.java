package chat;

import com.google.cloud.translate.*;

public class Translator {

    private final Translate translate;

    public Translator(String apiKey) {
        @SuppressWarnings("deprecation")
		TranslateOptions options = TranslateOptions.newBuilder().setApiKey(apiKey).build();
        translate = options.getService();
    }



	public String translateText(String originalText, String targetLanguage) {
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage(targetLanguage));
        return translation.getTranslatedText();
    }
}
