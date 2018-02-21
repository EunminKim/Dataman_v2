package com.club.term.dataman;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by eunmi on 2018-02-21.
 */

public class ListFileFragment extends ListFragment {

    public static ListFileFragment newInstance(String path) {
        ListFileFragment listFileFragment = new ListFileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        listFileFragment.setArguments(bundle);

        return listFileFragment;
    }

    public static ListFileFragment newInstance() {
        return newInstance(Environment.getRootDirectory().getAbsolutePath());
    }

    private String mRootPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootPath = getArguments().getString("path");
        getDir(mRootPath);
    }

    public ListFileFragment() {
    }

    private List<String> mItem = new ArrayList<>();
    private List<String> mPath = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_file_fragment, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getDir(mPath.get(position));
    }

    public void getDir(final String dirPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (dirPath.startsWith("smb://")) {
                    try {
                        SmbFile smbFile = new SmbFile(dirPath, new NtlmPasswordAuthentication(
                                null, "dataman", "1q2w3e"));
                        SmbFile[] smbFiles = smbFile.listFiles();
                        if (smbFiles == null || !smbFile.isDirectory()) return;

                        mItem.clear();
                        mPath.clear();

                        if (!TextUtils.equals(dirPath, mRootPath)) {
                            mItem.add("../");
                            mPath.add(smbFile.getParent());
                        }

                        for (SmbFile file : smbFiles) {
                            mPath.add(file.getPath());
                            mItem.add(file.getName());
                        }
                    } catch (MalformedURLException | SmbException e) {
                        e.printStackTrace();
                    }
                } else {
                    File f = new File(dirPath);
                    File[] files = f.listFiles();
                    if (files == null || !f.isDirectory()) return;

                    mItem.clear();
                    mPath.clear();

                    if (!TextUtils.equals(dirPath, mRootPath)) {
                        mItem.add("../");
                        mPath.add(f.getParent());
                    }

                    for (File file : files) {
                        mPath.add(file.getPath());
                        if (file.isDirectory()) {
                            mItem.add(file.getName() + "/");
                        } else {
                            mItem.add(file.getName());
                        }
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> fileList = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1, mItem);
                        setListAdapter(fileList);
                    }
                });
            }
        }).start();
    }
}
