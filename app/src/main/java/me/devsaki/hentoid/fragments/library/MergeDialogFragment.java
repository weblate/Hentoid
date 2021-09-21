package me.devsaki.hentoid.fragments.library;

import static androidx.core.view.ViewCompat.requireViewById;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.drag.ItemTouchCallback;
import com.mikepenz.fastadapter.drag.SimpleDragCallback;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter.utils.DragDropUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import io.reactivex.disposables.Disposable;
import me.devsaki.hentoid.R;
import me.devsaki.hentoid.database.CollectionDAO;
import me.devsaki.hentoid.database.ObjectBoxDAO;
import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.util.Helper;
import me.devsaki.hentoid.viewholders.IDraggableViewHolder;
import me.devsaki.hentoid.viewholders.TextItem;
import me.devsaki.hentoid.widget.DragSelectTouchListener;
import me.devsaki.hentoid.widget.FastAdapterPreClickSelectHelper;

public final class MergeDialogFragment extends DialogFragment implements ItemTouchCallback {

    private static final String KEY_CONTENTS = "contents";

    // === UI
    private RecyclerView recyclerView;
    private MaterialButton actionButton;

    private final ItemAdapter<TextItem<Content>> itemAdapter = new ItemAdapter<>();
    private final FastAdapter<TextItem<Content>> fastAdapter = FastAdapter.with(itemAdapter);
    private ItemTouchHelper touchHelper;
    private SelectExtension<TextItem<Content>> selectExtension;

    private DragSelectTouchListener mDragSelectTouchListener;

    // === VARIABLES
    private Parent parent;
    private long[] contentIds;

    private Disposable disposable;


    public static void invoke(
            @NonNull final FragmentActivity parent,
            @NonNull final List<Content> contentList) {
        MergeDialogFragment fragment = new MergeDialogFragment();

        Bundle args = new Bundle();
        args.putLongArray(KEY_CONTENTS, Helper.getPrimitiveLongArrayFromList(Stream.of(contentList).map(Content::getId).toList()));
        fragment.setArguments(args);

        fragment.show(parent.getSupportFragmentManager(), null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == getArguments()) throw new IllegalArgumentException("No arguments found");
        contentIds = getArguments().getLongArray(KEY_CONTENTS);

        parent = (Parent) getActivity();
    }

    @Override
    public void onDestroy() {
        parent = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.dialog_library_merge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        List<Content> contentList = loadContentList();
        itemAdapter.set(Stream.of(contentList).map(s -> new TextItem<>(s.getTitle(), s, false, true, touchHelper)).toList());

        recyclerView = requireViewById(rootView, R.id.list);

        // Activate drag & drop
        SimpleDragCallback dragCallback = new SimpleDragCallback(this);
        dragCallback.setNotifyAllDrops(true);
        touchHelper = new ItemTouchHelper(dragCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(fastAdapter);

        View actionButton = requireViewById(rootView, R.id.action_button);
        actionButton.setOnClickListener(v -> onActionClick());
    }

    private List<Content> loadContentList() {
        List<Content> result;
        CollectionDAO dao = new ObjectBoxDAO(requireContext());
        try {
            result = dao.selectContent(contentIds);
        } finally {
            dao.cleanup();
        }
        return result;
    }

    /**
     * Callback for any selection change (item added to or removed from selection)
     */
    private void onSelectionChanged() {
        Set<TextItem<Content>> selectedItems = selectExtension.getSelectedItems();
        int selectedCount = selectedItems.size();

        if (0 == selectedCount) {
            selectExtension.setSelectOnLongClick(true);
        } else {
            // TODO
        }
    }

    private void onActionClick() {
        // TODO
        this.dismiss();
    }


    // FastAdapter hooks

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {
        // Update  visuals
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(newPosition);
        if (vh instanceof IDraggableViewHolder) {
            ((IDraggableViewHolder) vh).onDropped();
        }
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        // Update visuals
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition); // change position
        return true;
    }

    @Override
    public void itemTouchStartDrag(RecyclerView.@NotNull ViewHolder viewHolder) {
        // Update visuals
        if (viewHolder instanceof IDraggableViewHolder) {
            ((IDraggableViewHolder) viewHolder).onDragged();
        }
    }

    @Override
    public void itemTouchStopDrag(RecyclerView.@NotNull ViewHolder viewHolder) {
        // Nothing
    }

    public interface Parent {
        void openUrl(@NonNull final String url);

        void updateBookmarkButton(boolean newValue);
    }
}
