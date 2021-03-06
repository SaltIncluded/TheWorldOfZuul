package worldofzuul.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.image.ImageView;
import worldofzuul.item.*;
import worldofzuul.parsing.Command;
import worldofzuul.parsing.CommandWord;
import worldofzuul.util.MessageHelper;

public class Field extends GameObject {
    private final static double minpH = 0.0;
    private final static double maxpH = 14.0;
    private final DoubleProperty pH = new SimpleDoubleProperty(7);
    private final FloatProperty water = new SimpleFloatProperty(20000);
    private final FloatProperty nutrition = new SimpleFloatProperty(10000);
    private final FloatProperty depletionRate = new SimpleFloatProperty(1);
    private final FloatProperty maxWater = new SimpleFloatProperty(20000);
    private final FloatProperty maxNutrition = new SimpleFloatProperty(20000);
    private Plant plant;
    private boolean ripePlantSeen = false;

    public Field() {
    }


    public Field(float water) {
        setWater(water);
    }

    public Field(float water, DoubleProperty pH) {
        this(water);
        this.setPH(pH.get());
    }


    public void addWater(float water) {
        if (getWater() + water <= getMaxWater()) {
            setWater(getWater() + water);
        }
    }

    @Override
    public Command[] update() {
        if (plant != null) {
            if (isPlantGrowing()) {
                float waterToAdd = 0;
                float nutritionToAdd = 0;

                if (plant.getWaterNeeded() > 0) {
                    waterToAdd = depleteWater(plant.getWaterDepletionRate());
                }
                if (plant.getNutritionNeeded() > 0) {
                    nutritionToAdd = depleteNutrition(plant.getNutritionDepletionRate());
                }
                plant.grow(waterToAdd, nutritionToAdd);

            }
            if (plant.isRipe() && !ripePlantSeen) {
                MessageHelper.Info.plantBecameRipe(plant.getName());

                ripePlantSeen = true;
            }
        }

        return super.update();
    }

    @Override
    public Command[] interact(Item item) {

        if (item instanceof Fertilizer) {
            MessageHelper.Item.usedItem(item.getName());
            return useFertilizer((Fertilizer) item);
        } else if (item instanceof Seed) {
            return useSeed((Seed) item);
        } else if (item instanceof Harvester) {
            return useHarvester((Harvester) item);
        } else if (item instanceof Irrigator) {
            MessageHelper.Item.usedItem(item.getName());
            useIrrigator((Irrigator) item);
            return null;
        } else if (item instanceof pHNeutralizers) {
            return usepHNeutralizers((pHNeutralizers) item);
        }

        return super.interact(item);
    }

    private Command[] useFertilizer(Fertilizer item) {
        Command[] commands = new Command[1];
        if (nutrition.get() + item.getConsumptionRate() < maxNutrition.get()) {
            nutrition.set(nutrition.get() + item.deplete());
        }


        if (item.getRemaining() == 0) {
            commands[0] = new Command(CommandWord.REMOVE_ITEM, null, item);
        }

        return commands;
    }

    private void useIrrigator(Irrigator item) {
        item.water(this);
    }

    private Command[] useSeed(Seed item) {
        if (plant != null) {
            MessageHelper.Item.alreadyPlanted();
            return null;
        }

        Command[] commands = new Command[1];

        if (item.getRemaining() > 0) {
            plantSeed(item);
        }

        if (item.getRemaining() == 0) {
            commands[0] = new Command(CommandWord.REMOVE_ITEM, null, item);
        }


        return commands;
    }

    private void plantSeed(Seed item) {
        MessageHelper.Item.usedItemOn(item.getName(), this.getClass().getSimpleName());
        plant = (item.useSeed());
        ripePlantSeen = false;

        plant.setImageView(getImageView());
        plant.playAnimation(GrowthStage.SEED);
        plant.stateProperty().addListener((observable, oldValue, newValue) -> plantStateChanged(newValue));

    }

    private void plantStateChanged(GrowthStage newValue) {
        plant.playAnimation(newValue);
    }

    private Command[] useHarvester(Harvester item) {
        Command[] commands = new Command[1];
        if (plant != null) {
            if (plant.isRipe()) {
                MessageHelper.Item.harvested(plant.getName());
                commands[0] = new Command(CommandWord.ADD_ITEM, null, item.harvest(plant));
                removePlant();

            } else if (plant.getState() == GrowthStage.DEAD) {
                removePlant();
            } else {
                MessageHelper.Item.unripePlant();
            }
        } else {
            MessageHelper.Item.noPlantOnField();
        }

        return commands;
    }

    public Command[] usepHNeutralizers(pHNeutralizers item) {
        Command[] commands = new Command[1];

        if (item.getRemaining() <= 0) {
            System.out.println("You do not have enough Neutralizers! You only got " + item.getRemaining() + " left");
            return commands;
        }

        double currentpH = getPH();

        if ((currentpH == maxpH && item.getpHChange() > 0) || (currentpH == minpH && item.getpHChange() < 0)) {
            System.out.println("You can not go below 0 or above 14 pH");
            return commands;
        }

        item.deplete();

        if (item.getRemaining() <= 0) {
            commands[0] = new Command(CommandWord.REMOVE_ITEM, null, item);
        }

        setPH(getPH() + item.getpHChange());

        if (currentpH < getPH()) {
            MessageHelper.Item.increasedpH(Double.toString(getPH()));
        } else {
            MessageHelper.Item.decreasedpH(Double.toString(getPH()));
        }

        return commands;
    }

    private void removePlant() {
        plant.stateProperty().removeListener((observable, oldValue, newValue) -> plantStateChanged(newValue));
        plant.playAnimation(null);
        plant.setImageView(null);
        this.plant = null;

    }

    private boolean isPlantGrowing() {
        return plant != null && !plant.isRipe();
    }

    private float depleteWater(float waterDepletionRate) {
        if (getWater() > getDepletionRate() * waterDepletionRate) {
            setWater(getWater() - getDepletionRate() * waterDepletionRate);
            return getDepletionRate() * waterDepletionRate;
        } else {
            return 0;
        }
    }

    private float depleteNutrition(float nutritionDepletionRate) {
        if (getNutrition() > getDepletionRate() * nutritionDepletionRate) {
            setNutrition(getNutrition() - getDepletionRate() * nutritionDepletionRate);
            return getDepletionRate() * nutritionDepletionRate;
        } else {
            return 0;
        }
    }

    public float getWater() {
        return water.get();
    }

    public void setWater(float water) {
        this.water.set(water);
    }

    public FloatProperty waterProperty() {
        return water;
    }

    public float getNutrition() {
        return nutrition.get();
    }

    public void setNutrition(float nutrition) {
        this.nutrition.set(nutrition);
    }

    public FloatProperty nutritionProperty() {
        return nutrition;
    }

    public float getDepletionRate() {
        return depletionRate.get();
    }

    public void setDepletionRate(float depletionRate) {
        this.depletionRate.set(depletionRate);
    }

    public FloatProperty depletionRateProperty() {
        return depletionRate;
    }


    public double getPH() {
        return pH.get();
    }

    public void setPH(double pH) {
        this.pH.set(pH);

        if (getPH() > maxpH) {
            setPH(maxpH);
        } else if (getPH() < minpH) {
            setPH(minpH);
        }
    }

    @JsonIgnore
    public DoubleProperty phProperty() {
        return pH;
    }

    public float getMaxWater() {
        return maxWater.get();
    }

    public void setMaxWater(float maxWater) {
        this.maxWater.set(maxWater);
    }

    @JsonIgnore
    public FloatProperty maxWaterProperty() {
        return maxWater;
    }

    public float getMaxNutrition() {
        return maxNutrition.get();
    }

    public void setMaxNutrition(float maxNutrition) {
        this.maxNutrition.set(maxNutrition);
    }

    @JsonIgnore
    public FloatProperty maxNutritionProperty() {
        return maxNutrition;
    }

    @Override
    public void setImageView(ImageView imageView) {
        super.setImageView(imageView);

        if (plant != null) {
            plant.setImageView(imageView);
            plant.playAnimation(plant.getState());
        }

    }
}
