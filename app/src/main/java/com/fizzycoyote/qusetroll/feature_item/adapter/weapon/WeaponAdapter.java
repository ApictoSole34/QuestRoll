package com.fizzycoyote.qusetroll.feature_item.adapter.weapon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_item.model.weapon.CombinedWeapon;

public class WeaponAdapter extends ListAdapter<CombinedWeapon, WeaponAdapter.ViewHolder> {

    private final OnWeaponClickListener listener;

    public interface OnWeaponClickListener {
        void onWeaponClick(CombinedWeapon weapon);
    }

    public WeaponAdapter(OnWeaponClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CombinedWeapon> DIFF =
            new DiffUtil.ItemCallback<CombinedWeapon>() {
                @Override public boolean areItemsTheSame(@NonNull CombinedWeapon a, @NonNull CombinedWeapon b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull CombinedWeapon a, @NonNull CombinedWeapon b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weapon, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDamage, tvType, tvRange, tvCustomBadge, tvSimpleBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_weapon_name);
            tvDamage = v.findViewById(R.id.tv_weapon_damage);
            tvType = v.findViewById(R.id.tv_weapon_type);
            tvRange = v.findViewById(R.id.tv_weapon_range);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
            tvSimpleBadge = v.findViewById(R.id.tv_simple_badge);
        }

        void bind(CombinedWeapon w, OnWeaponClickListener listener) {
            tvName.setText(w.name);

            StringBuilder dmg = new StringBuilder();
            if (w.damageDice != null && !w.damageDice.isEmpty()) dmg.append(w.damageDice);
            if (w.damageTypeName != null && !w.damageTypeName.isEmpty()) {
                if (dmg.length() > 0) dmg.append(" ");
                dmg.append(w.damageTypeName);
            }
            tvDamage.setText(dmg.length() > 0 ? dmg.toString() : "—");

            tvSimpleBadge.setText(w.isSimple ? "Simple" : "Martial");
            tvSimpleBadge.setVisibility(View.VISIBLE);

            if (w.range > 0) {
                String rangeText = (int) w.range + "/" + (int) w.longRange + " ft.";
                tvRange.setText(rangeText);
                tvRange.setVisibility(View.VISIBLE);
            } else {
                tvRange.setVisibility(View.GONE);
            }

            tvCustomBadge.setVisibility(w.isCustom ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> { if (listener != null) listener.onWeaponClick(w); });
        }
    }
}