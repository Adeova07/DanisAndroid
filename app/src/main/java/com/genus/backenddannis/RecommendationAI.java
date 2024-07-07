package com.genus.backenddannis;

import android.os.Build;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.genus.backenddannis.data.entity.Pendapatan;
import com.genus.backenddannis.data.entity.Pengeluaran;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendationAI {

    public SpannableString getRecommendation(List<Pengeluaran> pengeluaranList, List<Pendapatan> pendapatanList) {
        // Hitung total pengeluaran untuk setiap hari dalam seminggu
        Map<String, Double> pengeluaranPerHari = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());

        for (Pengeluaran pengeluaran : pengeluaranList) {
            double jumlah = pengeluaran.getJumlah();
            Date tanggal = pengeluaran.getTanggal();

            if (tanggal != null) {
                String hari = sdf.format(tanggal);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pengeluaranPerHari.put(hari, pengeluaranPerHari.getOrDefault(hari, 0.0) + jumlah);
                }
            }
        }

        // Hitung total pengeluaran dan pendapatan
        double totalPengeluaran = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalPengeluaran = pengeluaranList.stream().mapToDouble(Pengeluaran::getJumlah).sum();
        }
        double totalPendapatan = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalPendapatan = pendapatanList.stream().mapToDouble(Pendapatan::getJumlahP).sum();
        }

        // Perbandingkan total pengeluaran dan pendapatan
        double selisih = totalPendapatan - totalPengeluaran;

        // Buat rekomendasi berdasarkan perbandingan
        StringBuilder rekomendasi = new StringBuilder();

        for (String hari : getDaysOfWeek()) {
            double pengeluaranHari = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pengeluaranHari = pengeluaranPerHari.getOrDefault(hari, 0.0);
            }
            if (pengeluaranHari > 50000) {
                rekomendasi.append("Pengeluaran Anda pada hari ").append(hari)
                        .append(" sebesar ").append(pengeluaranHari)
                        .append(". Anda mungkin perlu mempertimbangkan untuk mengurangi pengeluaran pada hari tersebut.\n\n");
            } else {
                rekomendasi.append("Pengeluaran Anda pada hari ").append(hari)
                        .append(" sebesar ").append(pengeluaranHari)
                        .append(". Keuangan Anda saat ini masih stabil.\n\n");
            }
        }

        if (selisih < 0) {
            rekomendasi.append("Total pengeluaran Anda lebih besar daripada total pendapatan. Anda perlu mengurangi pengeluaran Anda.");
        } else {
            rekomendasi.append("Total pendapatan Anda lebih besar atau sama dengan total pengeluaran. Keuangan Anda dalam kondisi baik.");
        }

        return formatRecommendation(rekomendasi.toString());
    }

    private String[] getDaysOfWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String[] days = new String[7];
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i + 1);
            days[i] = sdf.format(calendar.getTime());
        }
        return days;
    }

    private SpannableString formatRecommendation(String recommendation) {
        SpannableString spannableString = new SpannableString(recommendation);
        String[] keywords = {"kategori", "Pengeluaran", "Keuangan"};
        for (String keyword : keywords) {
            int startIndex = recommendation.indexOf(keyword);
            while (startIndex != -1) {
                int endIndex = startIndex + keyword.length();
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                startIndex = recommendation.indexOf(keyword, endIndex);
            }
        }
        return spannableString;
    }
}
