package com.hyphenate.chatuidemo.section.group.viewmodels;

import android.app.Application;

import com.hyphenate.chatuidemo.common.livedatas.SingleSourceLiveData;
import com.hyphenate.chatuidemo.common.net.Resource;
import com.hyphenate.chatuidemo.common.repositories.EMContactManagerRepository;
import com.hyphenate.chatuidemo.common.repositories.EMGroupManagerRepository;
import com.hyphenate.easeui.domain.EaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class GroupPickContactsViewModel extends AndroidViewModel {
    private EMGroupManagerRepository repository;
    private EMContactManagerRepository contactRepository;
    private SingleSourceLiveData<Resource<List<String>>> groupMembers;
    private SingleSourceLiveData<Resource<List<EaseUser>>> contacts;
    private SingleSourceLiveData<Resource<Boolean>> addMembersObservable;

    public GroupPickContactsViewModel(@NonNull Application application) {
        super(application);
        repository = new EMGroupManagerRepository();
        contactRepository = new EMContactManagerRepository();
        groupMembers = new SingleSourceLiveData<>();
        contacts = new SingleSourceLiveData<>();
        addMembersObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<List<String>>> getGroupMembersObservable() {
        return groupMembers;
    }

    public void getGroupMembers(String groupId) {
        groupMembers.setSource(repository.getGroupMembersByName(groupId));
    }

    public LiveData<Resource<List<EaseUser>>> getContacts() {
        return contacts;
    }

    public void getAllContacts() {
        contacts.setSource(contactRepository.getContactList());
    }

    public LiveData<Resource<Boolean>> getAddMembersObservable() {
        return addMembersObservable;
    }

    public void addGroupMembers(boolean isOwner, String groupId, String[] members) {
        addMembersObservable.setSource(repository.addMembers(isOwner, groupId, members));
    }

}