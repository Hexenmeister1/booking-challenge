import { useState, type FormEvent } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import { apiRequest } from "../../api/client";
import { useResources } from "./useResources";

type BookingResponse = {
  id: number;
};

type BookingFormProps = {
  resourceId: number;
  resourceName: string;
  onClose: () => void;
  onCreated: () => void;
};

function BookingForm({ resourceId, resourceName, onClose, onCreated }: BookingFormProps) {
  const [bookedBy, setBookedBy] = useState("");
  const [startsAt, setStartsAt] = useState("");
  const [endsAt, setEndsAt] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (startsAt >= endsAt) {
      setError("Das Ende der Buchung muss nach ihrem Beginn liegen.");
      return;
    }

    setSaving(true);
    setError(null);
    try {
      await apiRequest<BookingResponse>("/api/bookings", {
        method: "POST",
        body: JSON.stringify({ resourceId, bookedBy, startsAt, endsAt }),
      });
      onCreated();
      onClose();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Die Buchung konnte nicht gespeichert werden.",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={submit}>
        <DialogTitle>Ressource buchen</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <TextField label="Ressource" value={resourceName} fullWidth disabled />
            <TextField
              label="Gebucht von"
              value={bookedBy}
              onChange={(event) => setBookedBy(event.target.value)}
              required
              autoFocus
              inputProps={{ maxLength: 120 }}
            />
            <TextField
              label="Beginn"
              type="datetime-local"
              value={startsAt}
              onChange={(event) => setStartsAt(event.target.value)}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Ende"
              type="datetime-local"
              value={endsAt}
              onChange={(event) => setEndsAt(event.target.value)}
              required
              InputLabelProps={{ shrink: true }}
            />
            {error && <Alert severity="error">{error}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={saving}>Abbrechen</Button>
          <Button type="submit" variant="contained" disabled={saving}>
            Buchen
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}

/**
 * The example feature: lists the bookable resources.
 *
 * It shows the three states a remote list has — loading, failed, loaded — because the failure case
 * is the one that usually gets forgotten.
 */
export function ResourceTable({ onBookingChanged = () => {} }: { onBookingChanged?: () => void }) {
  const { resources, loading, error, reload } = useResources();
  const [selectedResource, setSelectedResource] = useState<{ id: number; name: string } | null>(null);

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
        <CircularProgress aria-label="Ressourcen werden geladen" />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert
        severity="error"
        action={
          <Button color="inherit" size="small" onClick={reload}>
            Erneut versuchen
          </Button>
        }
      >
        {error}
      </Alert>
    );
  }

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table aria-label="Buchbare Ressourcen">
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Kategorie</TableCell>
            <TableCell>Standort</TableCell>
            <TableCell align="right">Plätze</TableCell>
            <TableCell align="right">Aktion</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {resources.map((resource) => (
            <TableRow key={resource.id} hover>
              <TableCell>{resource.name}</TableCell>
              <TableCell>
                <Chip label={resource.category} size="small" />
              </TableCell>
              <TableCell>{resource.location}</TableCell>
              <TableCell align="right">{resource.capacity}</TableCell>
              <TableCell align="right">
                <Button
                  size="small"
                  onClick={() => setSelectedResource({ id: resource.id, name: resource.name })}
                >
                  Buchen
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      {selectedResource && (
        <BookingForm
          resourceId={selectedResource.id}
          resourceName={selectedResource.name}
          onClose={() => setSelectedResource(null)}
          onCreated={onBookingChanged}
        />
      )}
    </TableContainer>
  );
}
