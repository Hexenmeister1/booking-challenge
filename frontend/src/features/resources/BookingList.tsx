import { useEffect, useState } from "react";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import IconButton from "@mui/material/IconButton";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { apiGet, apiRequest } from "../../api/client";
import { useResources } from "./useResources";

type Booking = {
  id: number;
  resourceId: number;
  resourceName: string;
  bookedBy: string;
  startsAt: string;
  endsAt: string;
};

export function BookingList({ refreshKey }: { refreshKey: number }) {
  const { resources } = useResources();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [resourceId, setResourceId] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [requestKey, setRequestKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    const params = new URLSearchParams();
    if (resourceId) params.set("resourceId", resourceId);
    if (from) params.set("from", from);
    if (to) params.set("to", to);
    const query = params.toString();

    apiGet<Booking[]>(`/api/bookings${query ? `?${query}` : ""}`, {
      signal: controller.signal,
    })
      .then((results) => {
        setBookings(results);
        setError(null);
        setLoading(false);
      })
      .catch((requestError: unknown) => {
        if (!controller.signal.aborted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "Die Buchungen konnten nicht geladen werden.",
          );
          setLoading(false);
        }
      })

    return () => controller.abort();
  }, [resourceId, from, to, refreshKey, requestKey]);

  async function cancel(id: number) {
    try {
      await apiRequest<void>(`/api/bookings/${id}`, { method: "DELETE" });
      setRequestKey((current) => current + 1);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Die Buchung konnte nicht storniert werden.",
      );
    }
  }

  return (
    <Box>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        alignItems={{ xs: "stretch", sm: "center" }}
        justifyContent="space-between"
        spacing={2}
        sx={{ mb: 2 }}
      >
        <Typography variant="h5" component="h2">Buchungen</Typography>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
          <TextField
            select
            size="small"
            label="Ressource"
            value={resourceId}
            onChange={(event) => {
              setLoading(true);
              setResourceId(event.target.value);
            }}
            sx={{ minWidth: 220 }}
          >
            <MenuItem value="">Alle Ressourcen</MenuItem>
            {resources.map((resource) => (
              <MenuItem key={resource.id} value={String(resource.id)}>{resource.name}</MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            label="Von"
            type="datetime-local"
            value={from}
            onChange={(event) => {
              setLoading(true);
              setFrom(event.target.value);
            }}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            size="small"
            label="Bis"
            type="datetime-local"
            value={to}
            onChange={(event) => {
              setLoading(true);
              setTo(event.target.value);
            }}
            InputLabelProps={{ shrink: true }}
          />
        </Stack>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress aria-label="Buchungen werden geladen" />
        </Box>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table aria-label="Buchungen">
            <TableHead>
              <TableRow>
                <TableCell>Ressource</TableCell>
                <TableCell>Gebucht von</TableCell>
                <TableCell>Beginn</TableCell>
                <TableCell>Ende</TableCell>
                <TableCell align="right">Aktion</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {bookings.map((booking) => (
                <TableRow key={booking.id} hover>
                  <TableCell>{booking.resourceName}</TableCell>
                  <TableCell>{booking.bookedBy}</TableCell>
                  <TableCell>{formatDateTime(booking.startsAt)}</TableCell>
                  <TableCell>{formatDateTime(booking.endsAt)}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Buchung stornieren">
                      <IconButton
                        aria-label={`Buchung von ${booking.bookedBy} stornieren`}
                        onClick={() => void cancel(booking.id)}
                        size="small"
                      >
                        <DeleteOutlineIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
              {bookings.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: "text.secondary" }}>
                    Keine Buchungen für diese Filter.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("de-DE", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}
