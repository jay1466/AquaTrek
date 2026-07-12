import React, { useEffect, useState } from 'react';
import { Box, Button, Chip, IconButton, Tooltip } from '@mui/material';
import { AddOutlined, EditOutlined, DeleteOutlined } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import PageHeader       from '@components/ui/PageHeader';
import DataTable        from '@components/ui/DataTable';
import SearchBar        from '@components/ui/SearchBar';
import StatusChip       from '@components/ui/StatusChip';
import ConfirmDialog    from '@components/ui/ConfirmDialog';
import BuildingFormModal from './BuildingFormModal';
import buildingApi      from '@api/buildingApi';
import { formatDate, getErrorMessage } from '@utils/helpers';
import { useAuth } from '@context/AuthContext';

function BuildingsPage() {
  const { user } = useAuth();
  const qc = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => { document.title = 'Buildings — AquaTrack'; }, []);

  const [page,    setPage]    = useState(0);
  const [size,    setSize]    = useState(20);
  const [keyword, setKeyword] = useState('');
  const [sortBy,  setSortBy]  = useState('name');
  const [sortDir, setSortDir] = useState('asc');
  const [formOpen,   setFormOpen]   = useState(false);
  const [editItem,   setEditItem]   = useState(null);
  const [deleteItem, setDeleteItem] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['buildings', page, size, keyword, sortBy, sortDir],
    queryFn:  () => buildingApi.search({ page, size, keyword, sort: `${sortBy},${sortDir}` }),
    select:   (r) => r.data,
  });

  const deleteMut = useMutation({
    mutationFn: (id) => buildingApi.delete(id),
    onSuccess:  () => {
      enqueueSnackbar('Building deleted.', { variant: 'success' });
      qc.invalidateQueries({ queryKey: ['buildings'] });
      setDeleteItem(null);
    },
    onError: (err) => enqueueSnackbar(getErrorMessage(err), { variant: 'error' }),
  });

  const canEdit = ['ADMIN', 'SUPER_ADMIN'].includes(user?.role);

  const columns = [
    {
      id: 'name', label: 'Building Name', sortable: true,
      render: (v) => <Box sx={{ fontWeight: 600 }}>{v}</Box>,
    },
    {
      id: 'code', label: 'Code',
      render: (v) => <Chip label={v} size="small" sx={{ fontWeight: 700, borderRadius: '6px' }} />,
    },
    { id: 'buildingType', label: 'Type',
      render: (v) => <Chip label={v} size="small" variant="outlined" sx={{ borderRadius: '6px', fontSize: 11 }} /> },
    { id: 'totalFloors', label: 'Floors', align: 'center' },
    { id: 'totalUnits',  label: 'Units',  align: 'center' },
    { id: 'status',      label: 'Status', render: (v) => <StatusChip status={v} /> },
    { id: 'createdAt',   label: 'Created', render: (v) => formatDate(v) },
    {
      id: 'actions', label: '', align: 'right',
      render: (_, row) => canEdit ? (
        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => { setEditItem(row); setFormOpen(true); }}>
              <EditOutlined fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" color="error" onClick={() => setDeleteItem(row)}>
              <DeleteOutlined fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ) : null,
    },
  ];

  return (
    <Box className="animate-fade-in">
      <PageHeader
        title="Buildings"
        subtitle="Manage buildings and blocks within your apartment society"
        breadcrumbs={[{ label: 'Dashboard', to: '/dashboard' }, { label: 'Buildings' }]}
        actions={canEdit && (
          <Button variant="contained" startIcon={<AddOutlined />}
                  onClick={() => { setEditItem(null); setFormOpen(true); }}
                  sx={{ borderRadius: '10px' }}>
            Add Building
          </Button>
        )}
      />

      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <SearchBar
          placeholder="Search by name or code…"
          onSearch={(v) => { setKeyword(v); setPage(0); }}
        />
      </Box>

      <DataTable
        columns={columns}
        rows={data?.content || []}
        loading={isLoading}
        pagination={{ page, size, total: data?.totalElements || 0 }}
        onPageChange={setPage}
        onSizeChange={(s) => { setSize(s); setPage(0); }}
        sortField={sortBy}
        sortDir={sortDir}
        onSort={(f, d) => { setSortBy(f); setSortDir(d); setPage(0); }}
        emptyTitle="No buildings found"
        emptyMessage="Add your first building using the button above."
      />

      <BuildingFormModal
        open={formOpen}
        onClose={() => { setFormOpen(false); setEditItem(null); }}
        building={editItem}
      />

      <ConfirmDialog
        open={Boolean(deleteItem)}
        title="Delete Building"
        message={`Delete "${deleteItem?.name}" (${deleteItem?.code})? This cannot be undone.`}
        confirmLabel="Delete"
        confirmColor="error"
        loading={deleteMut.isPending}
        onConfirm={() => deleteMut.mutate(deleteItem.id)}
        onCancel={() => setDeleteItem(null)}
      />
    </Box>
  );
}

export default BuildingsPage;
