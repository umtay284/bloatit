/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.model;

public class Demand extends Translatable implements Kudoable{
    private String title;
    private String description;
    private String specification;
    private int id;
    private Member author;
    private long karma;

    public Demand(int id, String title, String description, String specification, long karma, Member author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.specification = specification;
        this.karma = karma;
        this.author = author;
    }

    public String getDescription() {
        return this.description;
    }

    public int getId() {
        return this.id;
    }

    public String getSpecification() {
        return this.specification;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public void kudo(Member kudoer){
        this.karma += (long)(Math.log(kudoer.getKarma())/Math.log(2));
        this.author.receiveKudo(kudoer);
        kudoer.issueKudo();
    }

    @Override
    public void report(Member reporter){
        this.karma -= (long)(Math.log(reporter.getKarma())/Math.log(2));
        this.author.receiveReport(reporter);
        reporter.issueReport();
    }

    @Override
    public long getReputation(){
        return this.karma;
    }

    @Override
    public Member getAuthor(){
        return this.author;
    }
}