/*
 * Copyright (c) 2019 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.smartdevicelink.managers.screen.menu;

import java.util.List;

class SubCellCommandList {

	private RunScore listsScore;
	private String menuTitle;
	private Integer parentId;
	private List<MenuCell> oldList, newList;

	SubCellCommandList(String menuTitle, Integer parentId, RunScore listsScore, List<MenuCell> oldList, List<MenuCell> newList){
		setMenuTitle(menuTitle);
		setParentId(parentId);
		setListsScore(listsScore);
		setOldList(oldList);
		setNewList(newList);
	}

	private void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	Integer getParentId() {
		return parentId;
	}

	private void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	String getMenuTitle() {
		return menuTitle;
	}

	private void setListsScore(RunScore listsScore){
		this.listsScore = listsScore;
	}

	RunScore getListsScore() {
		return listsScore;
	}

	private void setOldList(List<MenuCell> oldList) {
		this.oldList = oldList;
	}

	List<MenuCell> getOldList() {
		return oldList;
	}

	private void setNewList(List<MenuCell> newList) {
		this.newList = newList;
	}

	List<MenuCell> getNewList() {
		return newList;
	}
}
