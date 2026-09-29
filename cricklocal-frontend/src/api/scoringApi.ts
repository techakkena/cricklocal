import { apiGet, apiPost, apiPostNoContent } from "./apiClient";
import type {
  BattingInningsResponse,
  BowlingInningsResponse,
  DeliveryResponse,
  InningsStateResponse,
  RecordDeliveryRequest,
  SetInningsStateRequest,
} from "./types";

export function getInningsState(
  inningsId: number,
): Promise<InningsStateResponse> {
  return apiGet<InningsStateResponse>(
    `/api/innings/${inningsId}/state`,
  );
}

export function setInningsState(
  inningsId: number,
  request: SetInningsStateRequest,
): Promise<InningsStateResponse> {
  return apiPost<InningsStateResponse>(
    `/api/innings/${inningsId}/state`,
    request,
  );
}

export function getDeliveries(
  inningsId: number,
): Promise<DeliveryResponse[]> {
  return apiGet<DeliveryResponse[]>(
    `/api/innings/${inningsId}/deliveries`,
  );
}

export function recordDelivery(
  inningsId: number,
  request: RecordDeliveryRequest,
): Promise<DeliveryResponse> {
  return apiPost<DeliveryResponse>(
    `/api/innings/${inningsId}/deliveries`,
    request,
  );
}

export function undoLastDelivery(
  inningsId: number,
): Promise<void> {
  return apiPostNoContent(
    `/api/innings/${inningsId}/deliveries/undo`,
  );
}

export function getBattingInnings(
  inningsId: number,
): Promise<BattingInningsResponse[]> {
  return apiGet<BattingInningsResponse[]>(
    `/api/innings/${inningsId}/batting`,
  );
}

export function getBowlingInnings(
  inningsId: number,
): Promise<BowlingInningsResponse[]> {
  return apiGet<BowlingInningsResponse[]>(
    `/api/innings/${inningsId}/bowling`,
  );
}