package worldofzuul.world;

import javafx.scene.image.Image;
import worldofzuul.item.Item;
import worldofzuul.parsing.Command;

import java.util.HashMap;
import java.util.LinkedList;

public class NPC extends GameObject {

    private LinkedList<Quest> quests = new LinkedList<>();
    private LinkedList<String> dialog = new LinkedList<>();

    public NPC() {
    }

    @Override
    public Command[] interact() {

        if (dialog.size() > 0) {
            String dialogText = dialog.getFirst();
            System.out.println(dialogText);

            dialog.remove(dialogText);
            dialog.add(dialogText);
        }

        return null;
    }

    @Override
    public Command[] interact(Item item) {

        if (quests.size() > 0) {
            Quest currentQuest = quests.getFirst();

            currentQuest.questCompleteProperty().removeListener((o, oV, nV) -> quests.remove(currentQuest));
            currentQuest.questCompleteProperty().addListener((o, oV, nV) -> quests.remove(currentQuest));

            if (currentQuest != null) {
                return currentQuest.turnIn(item);
            }
        }

        return super.interact(item);
    }

    public LinkedList<Quest> getQuests() {
        return quests;
    }

    public void setQuests(LinkedList<Quest> quests) {
        this.quests = quests;
    }

    public LinkedList<String> getDialog() {
        return dialog;
    }

    public void setDialog(LinkedList<String> dialog) {
        this.dialog = dialog;
    }

    @Override
    public void configureImages(HashMap<String, Image> images) {
        super.configureImages(images);

        for (Quest quest : quests) {
            quest.configureImages(images);
        }

    }
}
