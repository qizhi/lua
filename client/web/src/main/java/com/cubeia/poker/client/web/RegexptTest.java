package com.cubeia.poker.client.web;

/**
 * Created with IntelliJ IDEA.
 * User: game
 * Date: 2013-01-23
 * Time: 08:56
 * To change this template use File | Settings | File Templates.
 */
public class RegexptTest {

    public static void main(String... args) {
        String first = "c:/work/skins/default/less/base.less";
        //<mapper type="regexp" from="${less.dir.comp}/(.*)/less/(.*)\.less" to="${css.dir.comp}/\1/lcss/\2\.css" />
        System.out.println(first.replaceAll("(.*)/less/(.*)\\.less","$1/lcss/$2\\.css"));
    }
}
