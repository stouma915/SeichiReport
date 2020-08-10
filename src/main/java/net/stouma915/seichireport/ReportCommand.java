package net.stouma915.seichireport;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.util.Formatter;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ReportCommand extends CommandBase {

    private Step step = null;
    private String target = null;
    private String evidence = null;
    private String content = null;
    private String contact = null;

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/report";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("showRule")) {
            sendMessage(format("%s通報ルール", ChatColor.RED));
            sendMessage("・虚偽の通報は処罰対象となります。");
            sendMessage("・原則、頂いた通報に対する個別の返信はしておりません。");
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("showWarning")) {
            sendMessage(format("%sゲーム内の特定場所・特定時間を報告する時の注意", ChatColor.RED));
            sendMessage("以下の事項が全て明記されていないと、運営チームが場所や時間を特定出来ません。");
            sendMessage("・サーバー名(アルカディア？エデン？…)");
            sendMessage("・ワールド名(メインワールド？第1整地ワールド？第2整地ワールド？…)");
            sendMessage("・座標(X座標、Y座標、Z座標)");
            sendMessage("・時間(何月何日？何時何分頃？)");
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("aboutEvidence")) {
            sendMessage(format("%s証拠について", ChatColor.RED));
            sendMessage("運営チームが処罰を実行するためには、以下のような証拠が必要です。");
            sendMessage(format("%s動画による証拠が必要:", ChatColor.GREEN));
            sendMessage("・チート行為");
            sendMessage(format("%s動画証拠の用意:", ChatColor.GREEN));
            sendMessage(" ShadowPlayや他のデスクトップキャプチャソフトで撮影したのち、YouTubeに限定公開状態での投稿を行い、投稿した動画のURLを記入してください。");
            sendMessage(" 撮影の際、動画内の通報対象者のIDが不鮮明だと証拠になりませんので、近くで撮る、画質を上げる、視認性の高いフォントに変更するなど、工夫をして頂く必要があります。");
            sendMessage(format("%s画像による証拠が必要:", ChatColor.GREEN));
            sendMessage("・ゲーム内チャットでの違反行為");
            sendMessage("・他プレイヤーに対する迷惑行為(例：弓の乱射、詐欺行為)");
            sendMessage("・整地の心得違反");
            sendMessage(format("%s画像証拠の用意:", ChatColor.GREEN));
            sendMessage(" Minecraft標準のスクリーンショット機能で撮影した画像を画像共有サイトにアップロードしたのち、そのURLを記入してください。");
            sendMessage(" もしくはGyazo等の画面キャプチャ&共有ソフトを使用してもかまいません。その場合Minecraftのウインドウ全体をキャプチャしてください。部分的なキャプチャは証拠として不十分です。");
            sendMessage(" ※通報はGyazo等の他の人には画像公開されない(URLからしか開くことができない)サービスを使ってください。");
            sendMessage(" 撮影の際、画像内の通報対象者のIDが不鮮明だと証拠になりませんので、近くで撮る、画質を上げる、視認性の高いフォントに変更するなど、工夫をして頂く必要があります。");
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            step = null;
            target = null;
            evidence = null;
            content = null;
            contact = null;
            sendMessage(format("%sリセットしました。", ChatColor.GREEN));
            return;
        }
        if (step == null) {
            if (args.length != 1) {
                sendWrongUsage();
                return;
            }
            target = args[0];
            sendMessage(format("%sさんの通報を開始します。", target));
            sendMessage("通報する前に、以下の項目を確認してください(メッセージクリックで内容が表示されます):");
            sendReportRule();
            sendWarning();
            sendAboutEvidence();
            sendMessage(format("証拠動画または写真のURLを次のようにして追加してください:%s /report URL", ChatColor.GREEN));
            sendResetMessage();
            step = Step.EVIDENCE;
        } else if (step == Step.EVIDENCE) {
            if (args.length != 1) {
                sendWrongUsage();
                return;
            }
            evidence = args[0];
            sendMessage(format("%s証拠のURLを設定しました。", ChatColor.GREEN));
            sendMessage(format("通報内容を次のようにして追加してください:%s /report 通報内容", ChatColor.GREEN));
            sendResetMessage();
            step = Step.CONTENT;
        } else if (step == Step.CONTENT) {
            if (args.length == 0) {
                sendWrongUsage();
                return;
            }
            StringBuilder contentBuilder = new StringBuilder();
            for (String arg : args) contentBuilder.append(arg).append(" ");
            contentBuilder.setLength(contentBuilder.length() - 1);
            content = contentBuilder.toString();
            sendMessage(format("%s通報内容を設定しました。", ChatColor.GREEN));
            sendMessage(
                    format(
                            "連絡先TwitterIDまたはDiscordIDを次のようにして設定してください:%s /report 連絡先",
                            ChatColor.GREEN
                    )
            );
            sendResetMessage();
            step = Step.CONTACT;
        } else if (step == Step.CONTACT) {
            if (args.length != 1) {
                sendWrongUsage();
                return;
            }
            contact = args[0];
            sendMessage(format("%s連絡先を設定しました。", ChatColor.GREEN));
            sendMessage(
                    format(
                            "以下の情報であっているかを確認し、間違えていた場合は%s /report reset %sと入力し、やり直してください。",
                            ChatColor.GREEN,
                            ChatColor.RESET
                    )
            );
            sendMessage(format("%s証拠動画または画像のURL: %s%s", ChatColor.GREEN, ChatColor.RESET, evidence));
            sendMessage(format("%s通報内容: %s%s", ChatColor.GREEN, ChatColor.RESET, content));
            sendMessage(format("%s通報対象者: %s%s", ChatColor.GREEN, ChatColor.RESET, target));
            sendMessage(format("%s連絡先: %s%s", ChatColor.GREEN, ChatColor.RESET, contact));
            sendMessage(format("あっていた場合は、%s /report %sと入力してください。", ChatColor.GREEN, ChatColor.RESET));
            step = Step.CONFIRM;
        } else if (step == Step.CONFIRM) {
            if (args.length != 0) {
                sendWrongUsage();
                return;
            }
            sendMessage("もう一度以下の項目を確かめてください: ");
            sendReportRule();
            sendWarning();
            sendAboutEvidence();
            sendMessage(format("確認が終わったら、%s /report %sと入力してください。", ChatColor.GREEN, ChatColor.RESET));
            step = Step.SEND;
        } else if (step == Step.SEND) {
            if (args.length != 0) {
                sendWrongUsage();
                return;
            }
            String curlCommand =
                    format(
                            "curl https://docs.google.com/forms/d/e/1FAIpQLSfK9DQkUCD2qs8zATUuYIC3JuV3MyXRVCYjMb5g4g_hBUusSA/formResponse -d ifq -d entry.2052968875=%s -d entry.1686473642=%s -d entry.2090997674=%s -d entry.113501943=%s -d entry.891642373=%s",
                            evidence,
                            content,
                            target,
                            sender.getName(),
                            contact
                    );
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(curlCommand.split(" "));
                processBuilder.directory(new File("/"));
                processBuilder.start();
                sendMessage(format("%s送信しました。", ChatColor.GREEN));
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage(format("%s送信に失敗しました。", ChatColor.RED));
            }
            step = null;
            evidence = null;
            content = null;
            target = null;
            contact = null;
        }
    }

    private void sendMessage(String message) {
        TextComponentString textComponentString = new TextComponentString(message);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(textComponentString);
    }

    private void sendResetMessage() {
        sendMessage(
                format(
                        "入力を間違えた・最初からやり直したい場合は、%s /report reset %sと入力してください。",
                        ChatColor.GREEN,
                        ChatColor.RESET
                )
        );
    }

    private void sendWrongUsage() {
        sendMessage(format("%sコマンドの使用法が間違っています。", ChatColor.RED));
    }

    private void sendReportRule() {
        TextComponentString reportRule = new TextComponentString(
                format(
                        "%s%s%s通報ルール",
                        ChatColor.GREEN,
                        ChatColor.BOLD,
                        ChatColor.UNDERLINE
                )
        );
        reportRule.setStyle(
                new Style()
                        .setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/report showRule")
                        )
        );
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(reportRule);
    }

    private void sendWarning() {
        TextComponentString warning = new TextComponentString(
                format(
                        "%s%s%sゲーム内の特定場所・特定時間を報告する時の注意",
                        ChatColor.GREEN,
                        ChatColor.BOLD,
                        ChatColor.UNDERLINE
                ));
        warning.setStyle(
                new Style()
                        .setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/report showWarning")
                        )
        );
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(warning);
    }

    private void sendAboutEvidence() {
        TextComponentString aboutEvidence = new TextComponentString(
                format(
                        "%s%s%s証拠について",
                        ChatColor.GREEN,
                        ChatColor.BOLD,
                        ChatColor.UNDERLINE
                )
        );
        aboutEvidence.setStyle(
                new Style()
                        .setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/report aboutEvidence")
                        )
        );
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(aboutEvidence);
    }

    private String format(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    private enum Step {
        EVIDENCE,
        CONTENT,
        CONTACT,
        CONFIRM,
        SEND
    }

    private static class ChatColor {
        public static final String RED = "§c";
        public static final String GREEN = "§a";
        public static final String BOLD = "§l";
        public static final String UNDERLINE = "§n";
        public static final String RESET = "§r";
    }

}
