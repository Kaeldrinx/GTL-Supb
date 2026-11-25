package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class CatalystInput implements IPatternDetails.IInput {

    private final IPatternDetails.IInput delegate;

    private final AEKey key;
    private final long realMultiplier;

    private AEKey gtlsupb$virtualKey;

    private final Predicate<AEKey> selfSupplied;

    private final Predicate<AEKey> autoCraftable;

    public CatalystInput(IPatternDetails.IInput delegate, AEKey key, Predicate<AEKey> selfSupplied,
                         Predicate<AEKey> autoCraftable) {
        this.delegate = delegate;
        this.key = key;
        this.realMultiplier = delegate.getMultiplier();
        this.selfSupplied = selfSupplied;
        this.autoCraftable = autoCraftable;
    }

    private boolean gtlsupb$isAutoCraftable() {
        if (this.key == null || this.autoCraftable == null) return true;
        try {
            return this.autoCraftable.test(this.key);
        } catch (Throwable t) {
            return true;
        }
    }

    public boolean gtlsupb$isSelfSupplied() {
        if (this.key == null || this.selfSupplied == null) return true;
        try {
            return this.selfSupplied.test(this.key);
        } catch (Throwable t) {
            return true;
        }
    }

    @Override
    public long getMultiplier() {
        if (!gtlsupb$isSelfSupplied() && !gtlsupb$isAutoCraftable()) {

            CatalystPlanNotes.note(this.key);
        }
        return 1L;
    }

    public long gtlsupb$realMultiplier() {
        return this.realMultiplier;
    }

    public AEKey gtlsupb$key() {
        return this.key;
    }

    @Override
    public GenericStack[] getPossibleInputs() {
        AEKey virtualKey = gtlsupb$virtualKey();
        if (virtualKey != null) {
            return new GenericStack[] { new GenericStack(virtualKey, 1L) };
        }
        return this.delegate.getPossibleInputs();
    }

    @Nullable
    private AEKey gtlsupb$virtualKey() {
        if (this.gtlsupb$virtualKey == null && this.key != null) {
            try {
                ItemStack marker = Ae2PatternHelper.createVirtualMarker(this.key);
                if (!marker.isEmpty()) {
                    this.gtlsupb$virtualKey = AEItemKey.of(marker);
                }
            } catch (Throwable ignored) {}
        }
        return this.gtlsupb$virtualKey;
    }

    @Override
    public boolean isValid(AEKey input, Level level) {
        AEKey virtualKey = gtlsupb$virtualKey();
        if (virtualKey != null && virtualKey.equals(input)) return true;
        return this.delegate.isValid(input, level);
    }

    @Nullable
    @Override
    public AEKey getRemainingKey(AEKey template) {
        AEKey virtualKey = gtlsupb$virtualKey();
        if (virtualKey != null && virtualKey.equals(template)) return null;
        return this.delegate.getRemainingKey(template);
    }
}
