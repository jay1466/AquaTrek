import React, { useEffect, useState } from 'react';
import { Box, Button, Chip, IconButton, Tooltip } from '@mui/material';
import {
  AddOutlined, EditOutlined, DeleteOutlined,
  ToggleOnOutlined, ToggleOffOutlined,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { useAuth } from '@context/AuthContext';
import PageHeader    from '@components/ui/PageHeader';
import DataTable     from '@components/ui/DataTable';
import SearchBar     from '@components/ui/SearchBar';
import StatusChip    from '@components/ui/StatusChip';
import ConfirmDialog from '@components/ui/ConfirmDialog';
import ApartmentFormModal from './ApartmentFormModal';
import apartmentApi  from '@api/apartmentApi';
import { formatDate, getErrorMessage } from '@utils/helpers';

function ApartmentsPage() {
  const { user } = useAuth();
  const qc = useQueryClient();
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => { document.title = 'Apartments — AquaTrack'; }, []);

  // State
  const [page,    setPage]    = useState(0);
  const [size,    setSize]    = useState(20);
  const [keyword, setKeyword] = useState('');
  const [sortBy,  setSortBy]  = useState('name');
  const [sortDir, setSortDir] = useState('asc');
  const [formOpen,    setFormOpen]    = useState(false);
  const [editItem,    setEditItem]    = useState(null);
  const [deleteItem,  setDeleteItem]  = useState(null);

  // Query
  const { data, isLoading } = useQuery({
    queryKey: ['apartments', page, size, keyword, sortBy, sortDir],
    queryFn:  () => apartmentApi.getAll({ page, size, keyword, sort: `${sortBy},${sortDir}` }),
    select:   (r) => r.data,
  });

  // Delete mutation
  const deleteMut = useMutation({
    mutationFn: (id) => apartmentApi.delete(id),
    onSuccess:  () => {
      enqueueSnackbar('Apartment deleted.', { variant: 'success' });
      qc.invalidateQueries({ queryKey: ['apartments'] });
      setDeleteItem(null);
    },
    onError: (err) => enqueueSnackbar(getErrorMessage(err), { variant: 'error' }),
  });

  // Toggle active/inactive
  const toggleMut = useMutation({
    mutationFn: ({ id, active }) => active ? apartmentApi.deactivate(id) : apartmentApi.activate(id),
    onSuccess:  (_, vars) => {
      enqueueSnackbar(vars.active ? 'Apartment deactivated.' : 'Apartment activated.', { variant: 'success' });
      qc.invalidateQueries({ queryKey: ['apartments'] });
    },
    onError: (err) => enqueueSnackbar(getErrorMessage(err), { variant: 'error' }),
  });

  const handleEdit   = (row) => { setEditItem(row); setFormOpen(true); };
  const handleCreate = () => { setEditItem(null); setFormOpen(true); };
  const handleSort   = (field, dir) => { setSortBy(field); setSortDir(dir); setPage(0); };

  const rows    = data?.content    || [];
  const pageMeta = {
    page,
    size,
    total: data?.totalElements || 0,
    totalPages: data?.totalPages || 0,
  };

  const isSuperAdmin = user?.role === 'SUPER_ADMIN';

  const columns = [
    {
      id: 'name', label: 'Society Name', sortable: true,
      render: (v) => <Box sx={{ fontWeight: 600 }}>{v}</Box>,
    },
    { id: 'city',   label: 'City',     sortable: true  },
    { id: 'state',  label: 'State',    sortable: true  },
    { id: 'totalBuildings', label: 'Buildings', align: 'center' },
    { id: 'totalUnits',     label: 'Units',     align: 'center' },
    {
      id: 'subscriptionPlan', label: 'Plan',
      render: (v) => (
        <Chip
          label={v}
          size="small"
          variant="outlined"
          color={v === 'ENTERPRISE' ? 'primary' : v === 'PREMIUM' ? 'secondary' : 'default'}
          sx={{ fontWeight: 600, fontSize: 11, borderRadius: '6px' }}
        />
      ),
    },
    {
      id: 'status', label: 'Status',
      render: (v) => <StatusChip status={v} />,
    },
    {
      id: 'createdAt', label: 'Created',
      render: (v) => formatDate(v),
    },
    {
      id: 'actions', label: 'Actions', align: 'right',
      render: (_, row) => (
        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => handleEdit(row)}>
              <EditOutlined fontSize="small" />
            </IconButton>
          </Tooltip>
          {isSuperAdmin && (
            <>
              <Tooltip title={row.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}>
                <IconButton size="small"
                  onClick={() => toggleMut.mutate({ id: row.id, active: row.status === 'ACTIVE' })}>
                  {row.status === 'ACTIVE'
                    ? <ToggleOnOutlined fontSize="small" color="success" />
                    : <ToggleOffOutlined fontSize="small" color="action" />}
                </IconButton>
              </Tooltip>
              <Tooltip title="Delete">
                <IconButton size="small" color="error" onClick={() => setDeleteItem(row)}>
                  <DeleteOutlined fontSize="small" />
                </IconButton>
              </Tooltip>
            </>
          )}
        </Box>
      ),
    },
  ];

  return (
    <Box className="animate-fade-in">
      <PageHeader
        title="Apartment Societies"
        subtitle="Manage all registered apartment societies"
        breadcrumbs={[{ label: 'Dashboard', to: '/dashboard' }, { label: 'Apartments' }]}
        actions={
          isSuperAdmin && (
            <Button variant="contained" startIcon={<AddOutlined />}
                    onClick={handleCreate} sx={{ borderRadius: '10px' }}>
              Add Society
            </Button>
          )
        }
      />

      {/* Toolbar */}
      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <SearchBar
          placeholder="Search by name, city…"
          onSearch={(v) => { setKeyword(v); setPage(0); }}
        />
      </Box>

      <DataTable
        columns={columns}
        rows={rows}
        loading={isLoading}
        pagination={pageMeta}
        onPageChange={setPage}
        onSizeChange={(s) => { setSize(s); setPage(0); }}
        sortField={sortBy}
        sortDir={sortDir}
        onSort={handleSort}
        emptyTitle="No apartment societies found"
        emptyMessage="Create the first apartment society using the button above."
      />

      {/* Form modal */}
      <ApartmentFormModal
        open={formOpen}
        onClose={() => { setFormOpen(false); setEditItem(null); }}
        apartment={editItem}
      />

      {/* Delete confirm */}
      <ConfirmDialog
        open={Boolean(deleteItem)}
        title="Delete Apartment Society"
        message={`Are you sure you want to delete "${deleteItem?.name}"? This action cannot be undone.`}
        confirmLabel="Delete"
        confirmColor="error"
        loading={deleteMut.isPending}
        onConfirm={() => deleteMut.mutate(deleteItem.id)}
        onCancel={() => setDeleteItem(null)}
      />
    </Box>
  );
}

export default ApartmentsPage;
